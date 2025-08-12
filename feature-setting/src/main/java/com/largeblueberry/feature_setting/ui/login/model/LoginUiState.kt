package com.largeblueberry.feature_setting.ui.login.model

import com.google.firebase.auth.FirebaseUser
import com.largeblueberry.feature_setting.firebase.auth.UsageLimitResult

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val currentUser: FirebaseUser? = null,
    val usageLimitResult: UsageLimitResult? = null,
    val canUseWithoutLogin: Boolean = true
)
