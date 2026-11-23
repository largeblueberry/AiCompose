package com.largeblueberry.domain.model

/** 녹음 템포(BPM) 범위. 화면·저장·녹음이 같은 범위를 쓴다. */
object Tempo {
    const val MIN_BPM = 40
    const val MAX_BPM = 200
    const val DEFAULT_BPM = 100

    fun clamp(bpm: Int): Int = bpm.coerceIn(MIN_BPM, MAX_BPM)
}
