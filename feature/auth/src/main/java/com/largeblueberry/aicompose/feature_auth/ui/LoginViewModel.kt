package com.largeblueberry.aicompose.feature_auth.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.aicompose.feature_auth.dataLayer.repository.GoogleAuthDataSource
import com.largeblueberry.aicompose.feature_auth.domainLayer.model.AuthResultDomain
import com.largeblueberry.aicompose.feature_auth.domainLayer.usecase.LoginUseCase
import com.largeblueberry.aicompose.feature_auth.domainLayer.usecase.LogoutUseCase
import com.largeblueberry.aicompose.feature_auth.ui.model.AuthUiState
import com.largeblueberry.aicompose.feature_auth.ui.model.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.onSuccess


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logOutUseCase: LogoutUseCase,
    private val googleAuthDataSource: GoogleAuthDataSource
): ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _startGoogleSignInFlow = Channel<Intent>(Channel.BUFFERED)
    val startGoogleSignInFlow = _startGoogleSignInFlow.receiveAsFlow()

    fun onGoogleSignInClicked() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // 데이터 소스에서 signInIntent를 가져와 UI로 보냅니다.
                _startGoogleSignInFlow.send(googleAuthDataSource.getSignInIntent())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google 로그인 준비 중 오류 발생: ${e.localizedMessage}"
                )
            }
        }
    }

    // 이 함수는 ActivityResultLauncher에서 받은 원본 Intent 데이터를 받음.
    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            // 데이터 소스를 통해 Intent에서 GoogleSignInAccount를 추출합니다.
            val account = googleAuthDataSource.getSignedInAccountFromIntent(data)
            val idToken = account?.idToken

            if (idToken != null) {
                signInWithGoogle(idToken)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google 로그인 실패: ID 토큰을 가져올 수 없습니다."
                )
                _authUiState.value = AuthUiState.Error("ID 토큰 없음")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _authUiState.value = AuthUiState.Loading


            when (val result = loginUseCase(idToken)) {
                is AuthResultDomain.Success -> {
                    _authUiState.value = AuthUiState.Authenticated(result.user)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        currentUser = result.user,
                        errorMessage = null
                    )

                }
                is AuthResultDomain.Error -> {
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

    // 🚪 로그아웃
    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = logOutUseCase()

            result
                .onSuccess {
                    _authUiState.value = AuthUiState.NotAuthenticated
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { exception ->
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