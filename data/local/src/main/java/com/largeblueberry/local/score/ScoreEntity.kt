package com.largeblueberry.local.score

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sheet_music")
data class ScoreEntity(
    @PrimaryKey
    val id: String,               // UUID 또는 고유 ID
    val title: String?,           // 사용자가 직접 입력하거나 URL에서 추출
    val scoreUrl: String,         // 서버에서 받은 악보 URL
    val midiUrl: String?,
    val createdAt: Long
)