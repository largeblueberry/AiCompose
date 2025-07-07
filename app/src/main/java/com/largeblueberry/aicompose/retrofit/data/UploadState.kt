package com.largeblueberry.aicompose.retrofit.data

data class UploadState(
    val status: UploadStatus = UploadStatus.IDLE,
    val progress: Float = 0f,
    val message: String? = null,
    val url: String? = null,
    val recordId: Int? = null
)

enum class UploadStatus {
    IDLE, UPLOADING, SUCCESS, ERROR
}
/**
 * IDLE: 대기 상태
 */