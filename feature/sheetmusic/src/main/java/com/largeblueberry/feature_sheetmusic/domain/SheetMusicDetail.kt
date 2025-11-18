package com.largeblueberry.feature_sheetmusic.domain

data class SheetMusicDetail(
    val id: String,
    val title: String,
    val composer: String,
    val filePath: String,
    val createdAt: Long,
    val updatedAt: Long,
    val thumbnailPath: String? = null,
    val duration: Int? = null, // 초 단위
    val difficulty: String? = null
)
