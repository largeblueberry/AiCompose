package com.largeblueberry.feature_sheetmusic.domain

data class SheetMusic(
    val id: String,
    val title: String,
    val composer: String? = null,
    val scoreUrl: String? = null,
    val midiUrl: String? = null,
    val createdAt: String? = null,
    val duration: Int? = null, // 초 단위
    val key: String? = null,   // 조성
    val tempo: Int? = null     // BPM
)
