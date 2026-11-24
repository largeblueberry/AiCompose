package com.largeblueberry.music.basicpitch

import com.largeblueberry.music.basicpitch.BasicPitchConstants.ANNOT_N_FRAMES
import com.largeblueberry.music.basicpitch.BasicPitchConstants.AUDIO_N_SAMPLES
import com.largeblueberry.music.basicpitch.BasicPitchConstants.FFT_HOP
import com.largeblueberry.music.basicpitch.BasicPitchConstants.MAX_FREQ_IDX
import com.largeblueberry.music.basicpitch.BasicPitchConstants.MIDI_OFFSET
import com.largeblueberry.music.basicpitch.BasicPitchConstants.SAMPLE_RATE
import kotlin.math.ln
import kotlin.math.round

/** 프레임 단위 음표. end는 배타(exclusive)가 아니라 원본과 같은 의미의 프레임 인덱스다. */
data class FrameNoteEvent(
    val startFrame: Int,
    val endFrame: Int,
    val midi: Int,
    val amplitude: Float,
)

/** 초 단위 음표. Basic Pitch 출력의 최종 형태(템포·박자 정보 없음). */
data class NoteEvent(
    val startSec: Double,
    val endSec: Double,
    val midi: Int,
    val amplitude: Float,
)

/**
 * 원본 predict()의 파라미터와 같은 뜻. 기본값도 같다.
 * minNoteLenFrames 기본 11 = round(127.70ms / 1000 * 22050 / 256).
 */
data class NoteCreationParams(
    val onsetThreshold: Double = 0.5,
    val frameThreshold: Double = 0.3,
    val minNoteLenFrames: Int = 11,
    val inferOnsets: Boolean = true,
    val minFreqHz: Double? = null,
    val maxFreqHz: Double? = null,
    val melodiaTrick: Boolean = true,
    val energyTolerance: Int = 11,
) {
    init {
        require(onsetThreshold > 0.0) { "onsetThreshold must be > 0" }
        require(frameThreshold > 0.0) { "frameThreshold must be > 0" }
    }

    companion object {
        /** 사람 목소리(허밍) 범위로 제한한 앱 기본값. */
        val HUMMING = NoteCreationParams(minFreqHz = 60.0, maxFreqHz = 1100.0)
    }
}

/**
 * Basic Pitch 출력(note/onset) → 음표. 원본: basic_pitch/note_creation.py
 * `output_to_notes_polyphonic`, `get_infered_onsets`, `constrain_frequency`, `model_frames_to_time`
 * (Copyright 2022 Spotify AB, Apache License 2.0).
 *
 * 원본과 같은 결과를 내도록 옮겼다. 맞추려고 일부러 지킨 것:
 * - 원본의 remaining_energy·추정 onset은 numpy 기본값 float64다. 그래서 비교·계산을 Double로 한다.
 * - onset은 시간 내림차순, 같은 시간이면 bin 내림차순으로 처리한다(np.where 결과를 뒤집은 순서).
 * - melodia 단계는 평탄화 배열의 첫 argmax를 고른다(값 내림차순, 같으면 인덱스 오름차순).
 * - np.round는 짝수 반올림이다. kotlin.math.round도 짝수 반올림이다.
 * 바꾼 것: 원본은 추정 onset의 차분 최대가 0이면 NaN이 퍼진다. 여기서는 그 경우 차분을 0으로 둔다.
 */
object NoteCreation {

    fun toNotes(output: BasicPitchOutput, params: NoteCreationParams = NoteCreationParams()): List<NoteEvent> {
        val events = toFrameEvents(output, params)
        val times = modelFramesToTime(output.numFrames)
        return events.map { NoteEvent(times[it.startFrame], times[it.endFrame], it.midi, it.amplitude) }
    }

    fun toFrameEvents(
        output: BasicPitchOutput,
        params: NoteCreationParams = NoteCreationParams(),
    ): List<FrameNoteEvent> {
        val nFrames = output.numFrames
        val nBins = output.note.numBins
        if (nFrames == 0) return emptyList()

        // float32 값은 double로 정확히 옮겨진다. 원본도 계산 중간부터 float64다.
        val frames = DoubleArray(output.note.values.size) { output.note.values[it].toDouble() }
        var onsets = DoubleArray(frames.size) { output.onset.values[it].toDouble() }

        // 원본은 frames를 제자리에서 바꾸고, 이후 amplitude 평균도 바뀐 frames로 낸다.
        constrainFrequency(onsets, frames, nFrames, nBins, params.maxFreqHz, params.minFreqHz)
        if (params.inferOnsets) {
            onsets = inferredOnsets(onsets, frames, nFrames, nBins)
        }

        val remaining = frames.copyOf()
        val events = ArrayList<FrameNoteEvent>()

        // 시간축 엄격 극대(양 끝 프레임 제외) 중 임계값 이상 → 시간·bin 내림차순으로 처리
        for (t in nFrames - 2 downTo 1) {
            for (f in nBins - 1 downTo 0) {
                val v = onsets[t * nBins + f]
                if (v < params.onsetThreshold) continue
                if (!(v > onsets[(t - 1) * nBins + f] && v > onsets[(t + 1) * nBins + f])) continue
                val start = t
                if (start >= nFrames - 1) continue

                var i = start + 1
                var k = 0
                while (i < nFrames - 1 && k < params.energyTolerance) {
                    if (remaining[i * nBins + f] < params.frameThreshold) k++ else k = 0
                    i++
                }
                i -= k
                if (i - start <= params.minNoteLenFrames) continue

                for (r in start until i) {
                    remaining[r * nBins + f] = 0.0
                    if (f < MAX_FREQ_IDX) remaining[r * nBins + f + 1] = 0.0
                    if (f > 0) remaining[r * nBins + f - 1] = 0.0
                }
                events.add(FrameNoteEvent(start, i, f + MIDI_OFFSET, meanColumn(frames, nBins, f, start, i)))
            }
        }

        if (params.melodiaTrick) {
            melodia(remaining, frames, nFrames, nBins, params, events)
        }
        return events
    }

    private fun melodia(
        remaining: DoubleArray,
        frames: DoubleArray,
        nFrames: Int,
        nBins: Int,
        params: NoteCreationParams,
        events: MutableList<FrameNoteEvent>,
    ) {
        val thr = params.frameThreshold
        // 원본은 반복마다 전체 argmax를 구한다(첫 등장 우선). 남은 값은 처음 값 그대로이거나 0으로만 바뀌므로,
        // 임계값을 넘는 칸을 (값 내림차순, 인덱스 오름차순)으로 한 번 정렬해 두고 살아 있는 첫 칸을 고르면 같은 순서가 된다.
        val ordered = remaining.indices
            .filter { remaining[it] > thr }
            .sortedWith(compareByDescending<Int> { remaining[it] }.thenBy { it })
        var cursor = 0

        fun nextMax(): Int {
            while (cursor < ordered.size) {
                val idx = ordered[cursor]
                if (remaining[idx] > thr) return idx
                cursor++
            }
            return -1
        }

        while (true) {
            val idx = nextMax()
            if (idx < 0) break
            val iMid = idx / nBins
            val f = idx % nBins
            remaining[idx] = 0.0

            var i = iMid + 1
            var k = 0
            while (i < nFrames - 1 && k < params.energyTolerance) {
                if (remaining[i * nBins + f] < thr) k++ else k = 0
                remaining[i * nBins + f] = 0.0
                if (f < MAX_FREQ_IDX) remaining[i * nBins + f + 1] = 0.0
                if (f > 0) remaining[i * nBins + f - 1] = 0.0
                i++
            }
            val iEnd = i - 1 - k

            i = iMid - 1
            k = 0
            while (i > 0 && k < params.energyTolerance) {
                if (remaining[i * nBins + f] < thr) k++ else k = 0
                remaining[i * nBins + f] = 0.0
                if (f < MAX_FREQ_IDX) remaining[i * nBins + f + 1] = 0.0
                if (f > 0) remaining[i * nBins + f - 1] = 0.0
                i--
            }
            val iStart = i + 1 + k

            if (iEnd - iStart <= params.minNoteLenFrames) continue
            events.add(FrameNoteEvent(iStart, iEnd, f + MIDI_OFFSET, meanColumn(frames, nBins, f, iStart, iEnd)))
        }
    }

    /** 원본 get_infered_onsets(n_diff=2). 프레임 값이 크게 오르는 곳을 onset 후보로 더한다. */
    private fun inferredOnsets(onsets: DoubleArray, frames: DoubleArray, nFrames: Int, nBins: Int): DoubleArray {
        val nDiff = 2
        val diff = DoubleArray(frames.size)
        var maxDiff = 0.0
        for (t in 0 until nFrames) {
            for (f in 0 until nBins) {
                val idx = t * nBins + f
                var m = Double.POSITIVE_INFINITY
                for (n in 1..nDiff) {
                    val prev = if (t - n >= 0) frames[(t - n) * nBins + f] else 0.0
                    val d = frames[idx] - prev
                    if (d < m) m = d
                }
                if (m < 0.0 || t < nDiff) m = 0.0
                diff[idx] = m
                if (m > maxDiff) maxDiff = m
            }
        }
        // 원본: np.max(onsets)는 float32 스칼라. float32 값은 double로 정확히 표현되므로 그대로 쓴다.
        var maxOnset = Double.NEGATIVE_INFINITY
        for (v in onsets) if (v > maxOnset) maxOnset = v
        val out = DoubleArray(onsets.size)
        for (idx in onsets.indices) {
            val scaled = if (maxDiff > 0.0) maxOnset * diff[idx] / maxDiff else 0.0
            out[idx] = if (onsets[idx] >= scaled) onsets[idx] else scaled
        }
        return out
    }

    /** 원본 constrain_frequency. 범위 밖 bin의 onset·frame을 0으로. */
    private fun constrainFrequency(
        onsets: DoubleArray,
        frames: DoubleArray,
        nFrames: Int,
        nBins: Int,
        maxFreq: Double?,
        minFreq: Double?,
    ) {
        if (maxFreq != null) {
            val maxIdx = round(hzToMidi(maxFreq) - MIDI_OFFSET).toInt().coerceIn(0, nBins)
            for (t in 0 until nFrames) for (f in maxIdx until nBins) {
                onsets[t * nBins + f] = 0.0
                frames[t * nBins + f] = 0.0
            }
        }
        if (minFreq != null) {
            val minIdx = round(hzToMidi(minFreq) - MIDI_OFFSET).toInt().coerceIn(0, nBins)
            for (t in 0 until nFrames) for (f in 0 until minIdx) {
                onsets[t * nBins + f] = 0.0
                frames[t * nBins + f] = 0.0
            }
        }
    }

    /** librosa.hz_to_midi: 12 * (log2(f) - log2(440)) + 69 */
    internal fun hzToMidi(hz: Double): Double = 12.0 * (log2(hz) - log2(440.0)) + 69.0

    private fun log2(x: Double): Double = ln(x) / ln(2.0)

    /**
     * 원본 model_frames_to_time. 창 경계마다 생기는 어긋남을 보정하는 원본 식(매직 넘버 0.0018 포함)을 그대로 쓴다.
     * 주의: 원본은 보정 주기를 ANNOT_N_FRAMES(172)로 잡지만 unwrap 뒤 창당 프레임은 142다.
     * 30초 녹음에서 수십 ms 정도 어긋날 수 있다. 양자화의 위상 추정이 흡수하는 범위라 원본을 따른다.
     */
    fun modelFramesToTime(nFrames: Int): DoubleArray {
        val windowOffset = (FFT_HOP.toDouble() / SAMPLE_RATE) *
            (ANNOT_N_FRAMES - (AUDIO_N_SAMPLES.toDouble() / FFT_HOP)) + 0.0018
        return DoubleArray(nFrames) { f ->
            val original = (f.toLong() * FFT_HOP).toDouble() / SAMPLE_RATE
            val windowNumber = (f / ANNOT_N_FRAMES).toDouble()
            original - windowOffset * windowNumber
        }
    }

    /** frames[start:end, bin]의 평균(원본 np.mean, float32 결과). */
    private fun meanColumn(frames: DoubleArray, nBins: Int, bin: Int, start: Int, end: Int): Float {
        var sum = 0.0
        for (r in start until end) sum += frames[r * nBins + bin]
        return (sum / (end - start)).toFloat()
    }
}
