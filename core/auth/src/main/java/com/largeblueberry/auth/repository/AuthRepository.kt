package com.largeblueberry.auth.repository

import com.largeblueberry.auth.model.AuthResult
import com.largeblueberry.auth.model.UserCore
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    val authState: StateFlow<UserCore?>

    suspend fun signIn(idToken: String) : AuthResult

    suspend fun signOut(): Result<Unit>

    suspend fun getCurrentUser(): UserCore?

    suspend fun signInAnonymously(): Result<String>
}