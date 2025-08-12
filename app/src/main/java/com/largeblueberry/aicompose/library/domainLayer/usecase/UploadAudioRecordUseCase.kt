package com.largeblueberry.aicompose.library.domainLayer.usecase

import com.largeblueberry.aicompose.dataLayer.model.local.AudioRecordEntity
import com.largeblueberry.aicompose.library.dataLayer.repository.AudioUploadRepository

class UploadAudioRecordUseCase(private val repository: AudioUploadRepository) {
    suspend operator fun invoke(filepath : String) : Result<String> {
        return repository.uploadAudioFile(filepath)
    }
}