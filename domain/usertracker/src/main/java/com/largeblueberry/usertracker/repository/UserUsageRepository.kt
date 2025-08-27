package com.largeblueberry.usertracker.repository

interface UserUsageRepository {

    suspend fun getCurrentUploadCount(userId: String?): Int

    suspend fun incrementUploadCount(userId: String?)

    suspend fun resetUploadCount(userId: String?)

}

/**
 *
 * getCurrentUploadCount: 현재 사용자의 업로드 횟수를 반환, userId== null인 경우 익명 사용자로 간주
 */