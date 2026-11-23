package com.largeblueberry.music.basicpitch

/**
 * Spotify Basic Pitch(icassp_2022 nmp 모델) 상수. 원본: basic_pitch/constants.py, inference.py, note_creation.py (Apache 2.0).
 * 값을 바꾸면 모델 입출력과 어긋난다. 골든 테스트가 이 값들을 검증한다.
 */
object BasicPitchConstants {
    const val SAMPLE_RATE = 22_050
    const val FFT_HOP = 256

    /** 정수 나눗셈(22050 // 256 = 86). 원본도 정수다. */
    const val ANNOTATIONS_FPS = SAMPLE_RATE / FFT_HOP
    const val AUDIO_WINDOW_LENGTH_SEC = 2

    /** 모델 한 번 입력 길이(샘플). 22050 * 2 - 256 = 43,844 */
    const val AUDIO_N_SAMPLES = SAMPLE_RATE * AUDIO_WINDOW_LENGTH_SEC - FFT_HOP

    /** 모델 한 번 출력 프레임 수. 86 * 2 = 172 */
    const val ANNOT_N_FRAMES = ANNOTATIONS_FPS * AUDIO_WINDOW_LENGTH_SEC

    const val N_PITCHES = 88
    const val N_CONTOUR_BINS = 88 * 3

    /** 창 사이 겹치는 출력 프레임 수. 창마다 앞뒤 절반씩 버린다. */
    const val N_OVERLAPPING_FRAMES = 30
    const val OVERLAP_LEN = N_OVERLAPPING_FRAMES * FFT_HOP
    const val HOP_SIZE = AUDIO_N_SAMPLES - OVERLAP_LEN

    /** 88건반 첫 음(A0)의 MIDI 번호. 출력 bin 0 = MIDI 21. */
    const val MIDI_OFFSET = 21
    const val MAX_FREQ_IDX = 87
}
