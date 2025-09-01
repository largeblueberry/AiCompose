package com.largeblueberry.auth.model

import com.largeblueberry.auth.model.UserCore

// 인증 상태를 나타내는 sealed class
sealed class AuthUiState {
    object Loading : AuthUiState()
    object NotAuthenticated : AuthUiState()
    data class Authenticated(val user: UserCore) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}