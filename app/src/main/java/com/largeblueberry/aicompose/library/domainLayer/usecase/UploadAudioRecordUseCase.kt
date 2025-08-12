package com.largeblueberry.aicompose.library.domainLayer.usecase

import com.largeblueberry.aicompose.library.dataLayer.repository.AudioUploadRepository
import kotlin.Result

class UploadAudioRecordUseCase(private val repository: AudioUploadRepository) {
    // recordId 파라미터 제거
    suspend operator fun invoke(filepath : String) : Result<String> {
        return repository.uploadAudioFile(filepath)
    }
}