package com.largeblueberry.auth.repository

import com.largeblueberry.auth.model.AuthResult
import com.largeblueberry.auth.model.UserCore

interface AuthRepository {

    suspend fun signIn(idToken: String) : AuthResult

    suspend fun signOut(): Result<Unit>

    suspend fun getCurrentUser(): UserCore?
}