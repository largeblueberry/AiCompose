package com.largeblueberry.auth.model

sealed class AuthResult {
    data class Success(val user: UserCore) : AuthResult()
    data class Error(val message: String) : AuthResult()
}