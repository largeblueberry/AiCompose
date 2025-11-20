package com.largeblueberry.feature_sheetmusic.data

import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.network.model.response.SheetMusicDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

fun SheetMusicDto.toDomainModel(): SheetMusic {
    // ✅ 1. 현재 날짜와 시간을 가져옵니다.
    val currentDateTime = LocalDateTime.now()
    // ✅ 2. 원하는 날짜/시간 형식(포맷)을 정의합니다. (예: "2023-10-27 15:30")
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    // ✅ 3. 현재 시간을 정의한 포맷의 문자열로 변환합니다.
    val formattedDateTime = currentDateTime.format(formatter)

    return SheetMusic(
        id = UUID.randomUUID().toString(),
        title = "생성된 악보",
        composer = null, // ⚠️ 이 필드도 SheetMusic에서 String? (nullable)이어야 합니다.
        scoreUrl = this.scoreUrl,
        midiUrl = this.midiUrl,
        createdAt = formattedDateTime, // ✅ 4. null 대신 현재 시간 문자열을 할당합니다.
        duration = null, // ⚠️ 다른 null 필드들도 확인이 필요합니다.
        key = null,
        tempo = null
    )
}
