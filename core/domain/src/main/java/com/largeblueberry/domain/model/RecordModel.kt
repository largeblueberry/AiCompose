package com.largeblueberry.domain.model

data class RecordModel(
    val id: Int = 0,
    val filename: String,
    val filePath: String,
    val fileSize: Long,
    val duration: String,
    val createdAt: Long = System.currentTimeMillis(),
    /** 녹음할 때 고른 템포. 채보의 박 격자에 쓴다. */
    val bpm: Int,
    val beatsPerBar: Int = 4,
    /** WAV 샘플레이트(22,050 또는 44,100). */
    val sampleRate: Int,
)
