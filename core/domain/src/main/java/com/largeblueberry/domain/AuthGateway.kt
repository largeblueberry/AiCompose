package com.largeblueberry.domain

interface AuthGateway {

    suspend fun isLoggedIn(): Boolean

    suspend fun getCurrentUserId(): String?

    suspend fun getUploadLimitForUser(userId: String?): Int

}