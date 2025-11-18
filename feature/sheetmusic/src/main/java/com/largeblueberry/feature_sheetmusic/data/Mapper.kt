package com.largeblueberry.feature_sheetmusic.data

import com.largeblueberry.local.score.ScoreEntity
import com.largeblueberry.feature_sheetmusic.domain.SheetMusicDetail

fun ScoreEntity.toDomain(): SheetMusicDetail {
    return SheetMusicDetail(
        id = id,
        title = title,
        composer = composer,
        filePath = filePath,
        createdAt = createdAt,
        updatedAt = updatedAt,
        thumbnailPath = thumbnailPath,
        duration = duration,
        difficulty = difficulty
    )
}

fun SheetMusicDetail.toEntity(): ScoreEntity {
    return ScoreEntity(
        id = id,
        title = title,
        composer = composer,
        filePath = filePath,
        createdAt = createdAt,
        updatedAt = updatedAt,
        thumbnailPath = thumbnailPath,
        duration = duration,
        difficulty = difficulty
    )
}

fun List<ScoreEntity>.toDomain(): List<SheetMusicDetail> {
    return map { it.toDomain() }
}