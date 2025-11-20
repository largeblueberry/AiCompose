package com.largeblueberry.feature_sheetmusic.domain

data class SheetMusic(
    val id: String,
    val title: String,
    val composer: String?, // String이 아닌 String? (nullable)
    val scoreUrl: String,
    val midiUrl: String,
    val createdAt: String, // 이 필드는 위 코드처럼 실제 값을 넣어주므로 non-null 유지
    val duration: Int?,    // Int가 아닌 Int? (nullable)
    val key: String?,
    val tempo: Int?
)
