package com.largeblueberry.aicompose.feature_auth.ui.model

import com.largeblueberry.aicompose.feature_auth.domainLayer.model.UserDomain

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val currentUser: UserDomain? = null,
)