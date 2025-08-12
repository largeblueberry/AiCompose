package com.largeblueberry.feature_setting.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.largeblueberry.feature_setting.firebase.auth.AuthResult
import com.largeblueberry.feature_setting.firebase.auth.UsageLimitResult

interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    fun isSignedIn(): Boolean
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signOut(): Result<Unit>
    suspend fun checkUsageLimit(userId: String): UsageLimitResult
    suspend fun incrementUsage(userId: String): Boolean
}