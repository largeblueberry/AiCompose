package com.largeblueberry.local.score

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sheet_music")
data class ScoreEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val composer: String,
    val filePath: String,
    val createdAt: Long,
    val updatedAt: Long,
    val thumbnailPath: String? = null,
    val duration: Int? = null,
    val difficulty: String? = null
)