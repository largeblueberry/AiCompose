package com.largeblueberry.library.dataLayer.repository

import com.largeblueberry.remote.model.UploadResponse

interface AudioUploadRepository {
    suspend fun uploadAudioFile(filePath: String): Result<UploadResponse>
}