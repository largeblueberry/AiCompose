package com.largeblueberry.domain.usecase

import com.largeblueberry.domain.AuthGateway
import com.largeblueberry.domain.model.UploadAvailabilityResult
import com.largeblueberry.domain.repository.UserUsageRepository


class CheckUploadAvailabilityUseCase (
    private val userUsageRepository : UserUsageRepository,
    private val authGateway: AuthGateway
) {
    suspend fun invoke(): UploadAvailabilityResult {
        val isLoggedIn = authGateway.isLoggedIn()
        val currentUserId = authGateway.getCurrentUserId()

        val maxUpload = authGateway.getUploadLimitForUser(currentUserId)

        val currentUploads = userUsageRepository.getCurrentUploadCount(currentUserId)//현재 업로드 횟수

        return if(currentUploads < maxUpload) {
            UploadAvailabilityResult.Available(
                remainingUploads = maxUpload - currentUploads,
                currentUploads = currentUploads,
                maxUploads = maxUpload
            )
        } else {
            UploadAvailabilityResult.LimitReached(
                maxUploads = maxUpload,
                currentUploads = currentUploads
            )
        }

    }

    suspend fun uploadCounter(){
        val currentUserId = authGateway.getCurrentUserId()
        userUsageRepository.incrementUploadCount(currentUserId)
    }

    suspend fun resetCounter(){
        val currentUserId = authGateway.getCurrentUserId()
        userUsageRepository.resetUploadCount(currentUserId)
    }

}

/**
 * 로그인 한 사용자는 최대 5회 업로드 가능
 * 익명 사용자는 최대 1회 업로드 가능
 */