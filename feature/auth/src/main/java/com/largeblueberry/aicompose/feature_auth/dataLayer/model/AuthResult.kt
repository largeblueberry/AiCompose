package com.largeblueberry.aicompose.feature_auth.dataLayer.model

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}