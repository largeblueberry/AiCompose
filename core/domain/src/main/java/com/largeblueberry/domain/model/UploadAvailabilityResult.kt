package com.largeblueberry.domain.model

sealed class UploadAvailabilityResult{

    data class Available(
        val remainingUploads: Int,
        val currentUploads: Int,
        val maxUploads: Int
    ) : UploadAvailabilityResult()

    data class LimitReached(
        val maxUploads: Int,
        val currentUploads: Int
    ) : UploadAvailabilityResult()

    data class Error(val message: String) : UploadAvailabilityResult()
}

/**
 * val remainingUploads : 남은 횟수 저장 변수
 *
 * 업로드가 가능하며, 남은 횟수를 포함
 *
 * 업로드 한도가 초과되었으며, 최대 업로드 횟수를 포함
 *
 * 에러 상황 추가
 */