package com.largeblueberry.aicompose.feature_auth.ui

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.aicompose.feature_auth.dataLayer.repository.GoogleAuthDataSource
import com.largeblueberry.aicompose.feature_auth.domainLayer.usecase.CheckAuthStatusUseCase
import com.largeblueberry.aicompose.feature_auth.domainLayer.usecase.LoginUseCase
import com.largeblueberry.analyticshelper.AnalyticsHelper
import com.largeblueberry.auth.model.AuthResult
import com.largeblueberry.aicompose.feature_auth.domainLayer.usecase.LogoutUseCase
import com.largeblueberry.auth.model.AuthUiState
import com.largeblueberry.auth.model.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logOutUseCase: LogoutUseCase,
    private val googleAuthDataSource: GoogleAuthDataSource,
    private val analyticsHelper: AnalyticsHelper,
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase
): ViewModel() {

    private companion object {
        private const val TAG = "LoginViewModel"
    }

    // [수정] 프로퍼티 선언을 init 블록 위로 이동
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _startGoogleSignInFlow = Channel<Intent>(Channel.BUFFERED)
    val startGoogleSignInFlow = _startGoogleSignInFlow.receiveAsFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            // 현재 로그인된 사용자가 있는지 확인합니다.
            val currentUser = checkAuthStatusUseCase() // UseCase 실행
            if (currentUser != null) {
                // 로그인된 사용자가 있다면 Authenticated 상태로 변경
                _authUiState.value = AuthUiState.Authenticated(currentUser)
                _uiState.value = _uiState.value.copy(currentUser = currentUser)
            } else {
                // 로그인된 사용자가 없다면 NotAuthenticated 상태로 변경
                _authUiState.value = AuthUiState.NotAuthenticated
            }
        }
    }

    fun onGoogleSignInClicked() {
        Log.d(TAG, "onGoogleSignInClicked called")
        analyticsHelper.logEvent(name = "google_sign_in_clicked", params = emptyMap())

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                _startGoogleSignInFlow.send(googleAuthDataSource.getSignInIntent())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get Google sign-in intent", e)
                analyticsHelper.logEvent(
                    name = "google_sign_in_intent_failed",
                    params = mapOf("error_message" to (e.message ?: "Unknown error"))
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google 로그인 준비 중 오류 발생: ${e.localizedMessage}"
                )
            }
        }
    }

    fun handleGoogleSignInResult(data: Intent?) {
        Log.d(TAG, "handleGoogleSignInResult called")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val account = googleAuthDataSource.getSignedInAccountFromIntent(data)
            val idToken = account?.idToken

            if (idToken != null) {
                Log.d(TAG, "Successfully obtained ID token")
                signInWithGoogle(idToken)
            } else {
                Log.w(TAG, "Failed to get ID token from Google sign-in result")
                analyticsHelper.logEvent(name = "google_id_token_failed", params = emptyMap())

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google 로그인 실패: ID 토큰을 가져올 수 없습니다."
                )
                _authUiState.value = AuthUiState.Error("ID 토큰 없음")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        Log.d(TAG, "signInWithGoogle called")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _authUiState.value = AuthUiState.Loading

            when (val result = loginUseCase(idToken)) {
                is AuthResult.Success -> {
                    Log.i(TAG, "Sign-in with Google successful. User: ${result.user}")
                    analyticsHelper.logEvent(
                        name = "login_success",
                        params = mapOf("method" to "google")
                    )

                    _authUiState.value = AuthUiState.Authenticated(result.user)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        currentUser = result.user,
                        errorMessage = null
                    )
                }
                is AuthResult.Error -> {
                    Log.e(TAG, "Sign-in with Google failed: ${result.message}")
                    analyticsHelper.logEvent(
                        name = "login_failure",
                        params = mapOf(
                            "method" to "google",
                            "error_message" to result.message
                        )
                    )

                    _authUiState.value = AuthUiState.Error(result.message)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isLoginSuccess = false
                    )
                }
            }
        }
    }

    fun signOut() {
        Log.d(TAG, "signOut called")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = logOutUseCase()

            result
                .onSuccess {
                    Log.i(TAG, "Sign out successful")
                    analyticsHelper.logEvent(name = "sign_out", params = emptyMap())

                    _authUiState.value = AuthUiState.NotAuthenticated
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { exception ->
                    Log.e(TAG, "Sign out failed", exception)
                    analyticsHelper.logEvent(
                        name = "sign_out_failure",
                        params = mapOf("error_message" to (exception.message ?: "Unknown error"))
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                    _authUiState.value = AuthUiState.Error(exception.message ?: "로그아웃 실패")
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}