package com.largeblueberry.library.domainLayer.usecase

import com.largeblueberry.network.repository.AudioUploadRepository
import com.largeblueberry.network.model.request.UploadResponse
import kotlin.Result

class UploadAudioRecordUseCase(private val repository: AudioUploadRepository) {

    // 1. 반환 타입을 Result<String>에서 Result<UploadResponse>로 변경합니다.
    suspend operator fun invoke(filepath: String): Result<UploadResponse> {
        // 2. repository가 반환한 결과를 그대로 반환합니다.
        //    이제 map으로 변환할 필요가 없습니다.
        return repository.uploadAudioFile(filepath)
    }
}