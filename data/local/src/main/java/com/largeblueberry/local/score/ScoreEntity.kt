package com.largeblueberry.local.score

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sheet_music")
data class ScoreEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val scoreUrl: String,
    val midiUrl: String,
    val createdAt: Long, // 생성 시간을 Long 타입(타임스탬프)으로 저장
    val composer: String?,
    val duration: Int?,
    val key: String?,
    val tempo: Int?
)
