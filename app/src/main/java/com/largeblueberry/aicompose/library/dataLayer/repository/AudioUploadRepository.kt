package com.largeblueberry.aicompose.library.dataLayer.repository

interface AudioUploadRepository {
    suspend fun uploadAudioFile(filePath: String): Result<String>
}