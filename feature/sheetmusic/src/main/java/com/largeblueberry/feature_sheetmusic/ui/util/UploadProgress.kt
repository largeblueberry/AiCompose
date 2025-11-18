package com.largeblueberry.feature_sheetmusic.ui.util

sealed class UploadProgress {
    object None : UploadProgress()
    object Uploading : UploadProgress()
    object Converting : UploadProgress()
    object GeneratingScore : UploadProgress()
    object Completed : UploadProgress()
}