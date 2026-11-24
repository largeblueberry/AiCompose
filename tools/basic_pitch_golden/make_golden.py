"""Basic Pitch 골든 데이터 생성기.

Kotlin 포팅(:core:music의 note creation, 창 분할/unwrap)이 Python 원본과 같은 결과를 내는지
JVM 테스트로 비교하기 위한 데이터를 만든다.

실제 허밍 녹음이 없어서 '허밍풍' 합성 멜로디를 쓴다(배음·비브라토·엔벨로프·숨소리 노이즈·시작 지연).
합성 악보(정답)도 같이 저장해서, 이후 양자화·조성 추정 테스트의 정답으로 쓴다.

사용:
    .venv/Scripts/python.exe make_golden.py <출력 디렉터리>

출력(케이스마다):
    <case>.wav            16-bit PCM mono 22,050Hz. 모델 입력과 같은 샘플(WAV 리더 테스트 겸용)
    <case>.note.f32       unwrap된 note posteriorgram  [T, 88]  little-endian float32
    <case>.onset.f32      unwrap된 onset posteriorgram [T, 88]
    <case>.json           메타데이터, 정답 악보, 파라미터별 note events(프레임 인덱스와 초)
    <case>.raw_note.f32   (unwrap 테스트용, 일부 케이스만) 창별 원출력 [n_windows, 172, 88]
"""

import json
import os
import pathlib
import sys

import numpy as np
from scipy.io import wavfile

import basic_pitch
from basic_pitch import note_creation as nc
from basic_pitch.constants import (
    ANNOT_N_FRAMES,
    AUDIO_N_SAMPLES,
    AUDIO_SAMPLE_RATE,
    FFT_HOP,
)
from basic_pitch.inference import Model, get_audio_input, run_inference, unwrap_output

SR = AUDIO_SAMPLE_RATE
MODEL_PATH = pathlib.Path(basic_pitch.__file__).parent / "saved_models" / "icassp_2022" / "nmp.tflite"
N_OVERLAPPING_FRAMES = 30

# predict() 기본값과 같은 계산
DEFAULT_MIN_NOTE_LEN = int(np.round(127.70 / 1000 * (SR / FFT_HOP)))

# 비교할 파라미터 조합. "humming"은 앱에서 쓸 후보(사람 목소리 범위로 제한).
VARIANTS = {
    "default": dict(onset_thresh=0.5, frame_thresh=0.3, min_note_len=DEFAULT_MIN_NOTE_LEN,
                    infer_onsets=True, min_freq=None, max_freq=None, melodia_trick=True),
    "humming": dict(onset_thresh=0.5, frame_thresh=0.3, min_note_len=DEFAULT_MIN_NOTE_LEN,
                    infer_onsets=True, min_freq=60.0, max_freq=1100.0, melodia_trick=True),
    "no_melodia": dict(onset_thresh=0.6, frame_thresh=0.35, min_note_len=8,
                       infer_onsets=False, min_freq=None, max_freq=None, melodia_trick=False),
}


def midi_to_hz(m):
    return 440.0 * 2.0 ** ((m - 69) / 12.0)


def synth_hum(score, bpm, start_offset_s, tail_s, seed, legato=False, noise_db=-45.0, human=False):
    """score: [(midi or None(쉼표), 길이(박))]. 4/4, 한 박 = 4분음표. 길이 = 시작 지연 + 악보 + tail_s.
    human=True: 박자 흔들림(sd 20ms, ±40ms), 소리 길이 65~95%, 음정 오차(sd 15cent, ±25cent). 정답 악보는 그대로."""
    rng = np.random.default_rng(seed)
    total_s = start_offset_s + sum(b for _, b in score) * 60.0 / bpm + tail_s
    n = int(total_s * SR)
    out = np.zeros(n, dtype=np.float64)
    beat_s = 60.0 / bpm
    t = start_offset_s
    truth = []
    beat_pos = 0.0
    prev_f = None
    phase = 0.0
    for pitch, beats in score:
        dur = beats * beat_s
        if pitch is not None:
            truth.append({"midi": pitch, "startBeat": beat_pos, "beats": beats})
            jitter = float(np.clip(rng.normal(0, 0.02), -0.04, 0.04)) if human else 0.0
            i0 = max(0, int((t + jitter) * SR))
            # 실제로 소리 내는 길이: 레가토면 거의 꽉, 아니면 85%(human이면 65~95%)
            ratio = rng.uniform(0.65, 0.95) if human else (0.97 if legato else 0.85)
            sound = dur * ratio
            i1 = min(n, int((t + jitter + sound) * SR))
            m = i1 - i0
            if m > 0:
                tt = np.arange(m) / SR
                f_target = midi_to_hz(pitch)
                # 레가토면 이전 음에서 60ms 글리산도
                if legato and prev_f is not None:
                    glide = np.clip(tt / 0.06, 0, 1)
                    f = prev_f * (f_target / prev_f) ** glide
                else:
                    f = np.full(m, f_target)
                # 비브라토 5.5Hz ±25cent, 시작 150ms 뒤부터 서서히
                vib_depth = np.clip((tt - 0.15) / 0.2, 0, 1) * 25.0
                f = f * 2.0 ** (vib_depth * np.sin(2 * np.pi * 5.5 * tt + rng.uniform(0, 6.28)) / 1200.0)
                # 약간의 음정 흔들림(사람 목소리)
                cents = float(np.clip(rng.normal(0, 15.0), -25, 25)) if human else rng.normal(0, 4.0)
                f = f * 2.0 ** (cents / 1200.0)
                inst_phase = phase + 2 * np.pi * np.cumsum(f) / SR
                phase = inst_phase[-1]
                sig = np.zeros(m)
                for k in range(1, 7):
                    sig += (1.0 / k ** 1.3) * np.sin(k * inst_phase)
                attack = np.clip(tt / 0.03, 0, 1)
                release = np.clip((m / SR - tt) / 0.05, 0, 1)
                env = attack * release * rng.uniform(0.6, 1.0)
                out[i0:i1] += 0.25 * env * sig
                prev_f = f_target
        else:
            prev_f = None if not legato else prev_f
        t += dur
        beat_pos += beats
    out += 10 ** (noise_db / 20.0) * rng.normal(0, 1, n)
    out = np.clip(out, -1.0, 1.0)
    pcm = np.round(out * 32767).astype(np.int16)
    return pcm, truth


C4, D4, E4, F4, G4, A4, B4, C5, D5 = 60, 62, 64, 65, 67, 69, 71, 72, 74
FS4, G3, A3, B3 = 66, 55, 57, 59
R = None

CASES = {
    # 4분·8분·2분·쉼표가 섞인 C장조. 10초 남짓 → 창 여러 개.
    "c_major_100bpm": dict(
        bpm=100, offset=0.137, tail=0.4, seed=1, legato=False, raw=True, key="C major",
        score=[(C4, 1), (D4, 1), (E4, 1), (F4, 1),
               (G4, 1), (G4, 1), (A4, 0.5), (G4, 0.5), (E4, 1),
               (F4, 1), (E4, 1), (D4, 2),
               (C4, 1), (R, 1), (E4, 0.5), (G4, 0.5), (C5, 1),
               (B4, 1), (A4, 1), (G4, 2)]),
    # 레가토·글리산도·점음표·16분음표가 있는 G장조. 범위 A3~D5.
    "g_major_80bpm_legato": dict(
        bpm=80, offset=0.21, tail=0.4, seed=2, legato=True, raw=False, key="G major",
        score=[(G3, 1), (B3, 0.5), (D4, 0.5), (G4, 1.5), (FS4, 0.5),
               (E4, 0.25), (FS4, 0.25), (G4, 0.5), (A4, 1), (B4, 2),
               (D5, 1), (C5, 0.5), (B4, 0.5), (A4, 1), (G4, 1),
               (FS4, 1.5), (E4, 0.5), (D4, 2),
               (A3, 1), (D4, 1), (G4, 2)]),
    # 창 1개 안에 끝나는 짧은 녹음(단일 창 경로).
    "short_1s": dict(
        bpm=120, offset=0.05, tail=0.4, seed=3, legato=False, raw=True, key=None,
        score=[(E4, 0.5), (G4, 0.5), (C5, 1)]),
    # 사람처럼 흐트러진 D장조(박자·길이·음정 오차, 큰 기기 지연 0.32초, 노이즈 큼). 정확도로 평가한다.
    "humanized_d_major_90bpm": dict(
        bpm=90, offset=0.32, tail=0.4, seed=5, legato=False, raw=False, key="D major", human=True,
        noise_db=-38.0,
        score=[(62, 1), (66, 0.5), (69, 0.5), (74, 1), (73, 1),
               (71, 1), (69, 1), (66, 2),
               (67, 0.5), (66, 0.5), (64, 1), (61, 1), (64, 1),
               (62, 3), (R, 1),
               (69, 1), (71, 0.5), (73, 0.5), (74, 2),
               (69, 1.5), (66, 0.5), (62, 2)]),
    # 소리 없는 녹음(노이즈만). 음표가 거의 없어야 한다.
    "quiet_noise": dict(
        bpm=100, offset=0.0, tail=0.4, seed=4, legato=False, raw=False, key=None,
        score=[(R, 4)], noise_db=-40.0),
}


def write_f32(path, arr):
    np.ascontiguousarray(arr, dtype="<f4").tofile(path)


def raw_windows(wav_path):
    """run_inference와 같은 창 분할로 창별 원출력을 얻는다(unwrap 테스트용)."""
    model = Model(MODEL_PATH)
    overlap_len = N_OVERLAPPING_FRAMES * FFT_HOP
    hop_size = AUDIO_N_SAMPLES - overlap_len
    notes = []
    original_length = None
    for x, _, original_length in get_audio_input(wav_path, overlap_len, hop_size):
        notes.append(model.predict(x)["note"])
    return np.concatenate(notes), original_length


def main(out_dir):
    out = pathlib.Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)
    model = Model(MODEL_PATH)
    for name, c in CASES.items():
        pcm, truth = synth_hum(c["score"], c["bpm"], c["offset"], c["tail"], c["seed"],
                               legato=c["legato"], noise_db=c.get("noise_db", -45.0),
                               human=c.get("human", False))
        wav_path = out / f"{name}.wav"
        wavfile.write(wav_path, SR, pcm)

        output = run_inference(str(wav_path), model)
        frames, onsets = output["note"], output["onset"]
        write_f32(out / f"{name}.note.f32", frames)
        write_f32(out / f"{name}.onset.f32", onsets)
        n_frames = frames.shape[0]
        times = nc.model_frames_to_time(n_frames)

        variants = {}
        for vname, p in VARIANTS.items():
            # output_to_notes_polyphonic은 입력 배열을 제자리에서 바꾸므로 복사본을 넘긴다
            ev = nc.output_to_notes_polyphonic(
                frames.copy(), onsets.copy(),
                onset_thresh=p["onset_thresh"], frame_thresh=p["frame_thresh"],
                min_note_len=p["min_note_len"], infer_onsets=p["infer_onsets"],
                max_freq=p["max_freq"], min_freq=p["min_freq"], melodia_trick=p["melodia_trick"],
            )
            variants[vname] = {
                "params": p,
                # 순서도 비교 대상: Python이 만든 순서 그대로
                "events": [
                    {"startFrame": int(s), "endFrame": int(e), "midi": int(m), "amplitude": float(a),
                     "startSec": float(times[s]), "endSec": float(times[e])}
                    for s, e, m, a in ev
                ],
            }

        meta = {
            "case": name,
            "sampleRate": SR,
            "numSamples": int(len(pcm)),
            "numFrames": int(n_frames),
            "numPitches": int(frames.shape[1]),
            "constants": {
                "fftHop": FFT_HOP, "audioNSamples": AUDIO_N_SAMPLES, "annotNFrames": ANNOT_N_FRAMES,
                "nOverlappingFrames": N_OVERLAPPING_FRAMES,
            },
            "frameTimesSec": [float(x) for x in times],
            "truth": {"bpm": c["bpm"], "timeSignature": "4/4", "startOffsetSec": c["offset"],
                      "key": c["key"], "notes": truth},
            "variants": variants,
        }

        if c["raw"]:
            raw, original_length = raw_windows(str(wav_path))
            write_f32(out / f"{name}.raw_note.f32", raw)
            check = unwrap_output(raw, original_length, N_OVERLAPPING_FRAMES)
            assert np.array_equal(check, frames), "unwrap 재현 실패"
            meta["rawWindows"] = int(raw.shape[0])

        with open(out / f"{name}.json", "w", encoding="utf-8") as f:
            json.dump(meta, f, ensure_ascii=False, indent=1)
        counts = {k: len(v["events"]) for k, v in variants.items()}
        print(f"{name}: frames={n_frames} truthNotes={len(truth)} events={counts}")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "out")
