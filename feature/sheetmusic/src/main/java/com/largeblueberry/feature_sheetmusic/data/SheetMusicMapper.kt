package com.largeblueberry.feature_sheetmusic.data

import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.network.model.response.SheetMusicDto
import java.util.UUID

fun SheetMusicDto.toDomainModel(): SheetMusic {
    return SheetMusic(
        id = UUID.randomUUID().toString(), // 임시 ID 생성
        title = "생성된 악보", // 기본값
        composer = null,
        scoreUrl = this.scoreUrl, // 실제로 받아오는 데이터
        midiUrl = null,
        createdAt = null,
        duration = null,
        key = null,
        tempo = null
    )
}