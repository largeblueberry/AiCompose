package com.largeblueberry.aicompose.feature_auth.domainLayer.model

sealed class AuthResultDomain {
    data class Success(val user: UserDomain) : AuthResultDomain()
    data class Error(val message: String) : AuthResultDomain()
}