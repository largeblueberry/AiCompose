package com.largeblueberry.record.domain.model

data class RecordModel(
    val id: Int = 0,
    val filename: String,
    val filePath: String,
    val fileSize: Long,
    val duration: String,
    val createdAt: Long = System.currentTimeMillis()
)
