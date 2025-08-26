package com.largeblueberry.aicompose.feature_auth.ui.model

import com.largeblueberry.aicompose.feature_auth.domainLayer.model.UserDomain

// 인증 상태를 나타내는 sealed class
sealed class AuthUiState {
    object Loading : AuthUiState()
    object NotAuthenticated : AuthUiState()
    data class Authenticated(val user: UserDomain) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}