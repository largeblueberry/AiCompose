package com.largeblueberry.feature_setting.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.largeblueberry.feature_setting.domain.usecase.CheckUsageLimitUseCase
import com.largeblueberry.feature_setting.domain.usecase.GetCurrentUserUseCase
import com.largeblueberry.feature_setting.domain.usecase.IncrementUsageUseCase
import com.largeblueberry.feature_setting.domain.usecase.LoginUseCase
import com.largeblueberry.feature_setting.domain.usecase.LogoutUseCase
import com.largeblueberry.feature_setting.firebase.auth.AuthResult
import com.largeblueberry.feature_setting.firebase.auth.AuthState
import com.largeblueberry.feature_setting.firebase.auth.GoogleAuthManager
import com.largeblueberry.feature_setting.ui.login.model.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val checkUsageLimitUseCase: CheckUsageLimitUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    // 🔍 현재 사용자 상태 확인
    private fun checkCurrentUser() {
        val currentUser = getCurrentUserUseCase()
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
            _uiState.value = _uiState.value.copy(
                currentUser = currentUser,
                isLoginSuccess = true
            )
            // 로그인된 사용자의 사용량 확인
            checkUsageLimit(currentUser.uid)
        } else {
            _authState.value = AuthState.NotAuthenticated
            _uiState.value = _uiState.value.copy(
                canUseWithoutLogin = checkUsageLimitUseCase.canUseWithoutLogin()
            )
        }
    }

    // 🚀 구글 로그인
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _authState.value = AuthState.Loading

            when (val result = loginUseCase(idToken)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Authenticated(result.user)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        currentUser = result.user,
                        errorMessage = null
                    )

                    // 🔥 로그인 성공 후 사용량 확인
                    checkUsageLimit(result.user.uid)
                }

                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isLoginSuccess = false
                    )
                }
            }
        }
    }

    // 🚪 로그아웃
    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            logoutUseCase()
                .onSuccess {
                    _authState.value = AuthState.NotAuthenticated
                    _uiState.value = LoginUiState(
                        canUseWithoutLogin = checkUsageLimitUseCase.canUseWithoutLogin()
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }

    // 📊 사용량 한도 확인
    fun checkUsageLimit(userId: String) {
        viewModelScope.launch {
            val result = checkUsageLimitUseCase(userId)
            _uiState.value = _uiState.value.copy(
                usageLimitResult = result
            )
        }
    }


    // 🔄 에러 메시지 클리어
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // 🔔 사용량 경고 메시지
    fun getUsageWarningMessage(): String? {
        val result = _uiState.value.usageLimitResult
        return if (result != null && result.remainingCount <= 3) {
            "⚠️ 남은 사용량이 ${result.remainingCount}회입니다. 회원이 되시면 더 많이 사용할 수 있어요!"
        } else null
    }
}