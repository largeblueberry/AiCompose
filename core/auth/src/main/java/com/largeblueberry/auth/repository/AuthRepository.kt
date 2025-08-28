package com.largeblueberry.auth.repository

import com.largeblueberry.auth.model.AuthResult

interface AuthRepository {

    suspend fun signIn(idToken: String) : AuthResult

    suspend fun signOut(): Result<Unit>
}