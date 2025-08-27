package com.largeblueberry.usertracker.repository

interface AuthGateway {

    suspend fun isLoggedIn(): Boolean

    suspend fun getCurrentUserId(): String?

    suspend fun getUploadLimitForUser(userId: String?): Int

}