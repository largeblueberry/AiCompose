package com.largeblueberry.library.dataLayer.repository

interface AudioUploadRepository {
    suspend fun uploadAudioFile(filePath: String): Result<String>
}