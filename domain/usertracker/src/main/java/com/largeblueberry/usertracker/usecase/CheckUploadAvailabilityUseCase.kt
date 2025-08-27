package com.largeblueberry.usertracker.usecase

import com.largeblueberry.usertracker.model.UploadAvailabilityResult
import com.largeblueberry.usertracker.repository.UserUsageRepository

class CheckUploadAvailabilityUseCase(
    private val userUsageRepository : UserUsageRepository
) {

    private val LOGGED_IN_MAX_UPLOADS = 5

    private val ANONYMOUS_MAX_UPLOADS = 1

    suspend fun invoke(userId: String?): UploadAvailabilityResult{
        val isLoggedIn = userUsageRepository.isLoggedIn()

        val currentUploads = userUsageRepository.getCurrentUploadCount(userId)//현재 업로드 횟수

        val maxUpload = if(isLoggedIn) LOGGED_IN_MAX_UPLOADS else ANONYMOUS_MAX_UPLOADS

        return if(currentUploads < maxUpload){
            UploadAvailabilityResult.Available(remainingUploads = maxUpload - currentUploads)
            //업로드 가능, 남은 업로드 횟수 포함
        }else {
            UploadAvailabilityResult.LimitReached(maxUploads = maxUpload)
            //업로드 한도 초과
        }

    }

    suspend fun uploadCounter(userId: String?){
        userUsageRepository.incrementUploadCount(userId)
    }

    suspend fun resetCounter(userId: String?){
        userUsageRepository.resetUploadCount(userId)
    }

}

/**
 * 로그인 한 사용자는 최대 5회 업로드 가능
 * 익명 사용자는 최대 1회 업로드 가능
 */