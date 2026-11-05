package com.largeblueberry.domain.model

data class LibraryModel(
    val id: Int = 0,
    val filename: String,
    val filePath: String,
    val fileSize: Long,
    val duration: String,
    val createdAt: Long = System.currentTimeMillis()
)
