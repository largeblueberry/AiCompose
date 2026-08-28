package com.largeblueberry.library.domainLayer.usecase

import com.largeblueberry.network.model.response.UploadResponse
import com.largeblueberry.network.repository.AudioUploadRepository
import kotlin.Result

class UploadAudioRecordUseCase(private val repository: AudioUploadRepository) {

    // 1. 반환 타입을 Result<String>에서 Result<UploadResponse>로 변경
    suspend operator fun invoke(filepath: String): Result<UploadResponse> {
        // 2. repository가 반환한 결과를 그대로 반환
        return repository.uploadAudioFile(filepath)
    }
}