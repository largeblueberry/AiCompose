package com.largeblueberry.network.repository

import com.largeblueberry.network.model.request.UploadResponse

interface AudioUploadRepository {
    suspend fun uploadAudioFile(filePath: String): Result<UploadResponse>
}