package com.largeblueberry.aicompose.record.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_records")
data class AudioRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filename: String,
    val filePath: String,
    val timestamp: Long,
    val duration: String
)