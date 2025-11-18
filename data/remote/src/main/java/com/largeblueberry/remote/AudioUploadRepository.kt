package com.largeblueberry.remote

import com.largeblueberry.remote.model.UploadResponse

interface AudioUploadRepository {
    suspend fun uploadAudioFile(filePath: String): Result<UploadResponse>
}