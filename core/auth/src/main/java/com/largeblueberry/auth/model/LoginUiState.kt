package com.largeblueberry.auth.model

import com.largeblueberry.auth.model.UserCore

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val currentUser: UserCore? = null
)