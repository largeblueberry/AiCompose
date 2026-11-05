package com.largeblueberry.library.ui.model

data class UploadState(
    val status: UploadStatus = UploadStatus.IDLE,
    val progress: Float = 0f,
    val message: String? = null,
    val scoreUrl: String? = null,
    val midiUrl: String? = null,
    val recordId: Int? = null
)

enum class UploadStatus {
    IDLE, UPLOADING, SUCCESS, ERROR
}
