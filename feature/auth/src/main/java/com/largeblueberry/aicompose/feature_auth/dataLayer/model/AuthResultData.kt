package com.largeblueberry.aicompose.feature_auth.dataLayer.model

sealed class AuthResultData {
    data class Success(val user: User) : AuthResultData()
    data class Error(val message: String) : AuthResultData()
}