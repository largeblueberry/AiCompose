package com.largeblueberry.setting.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.analyticshelper.AnalyticsHelper
import com.largeblueberry.auth.model.AuthUiState
import com.largeblueberry.auth.model.LoginUiState
import com.largeblueberry.auth.repository.AuthRepository
import com.largeblueberry.setting.domain.usecase.LogoutUseCaseImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val logOutUseCase: LogoutUseCaseImpl,
    private val authRepository: AuthRepository,
    private val analyticsHelper: AnalyticsHelper,

) : ViewModel() {

    private companion object {
        private const val TAG = "SettingViewModel"
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    // AuthRepository의 authState를 구독하여 실시간으로 상태 반영
    val authUiState: StateFlow<AuthUiState> = authRepository.authState
        .map { userCore ->
            when (userCore) {
                null -> {
                    Log.d(TAG, "Auth state: Not authenticated")
                    AuthUiState.NotAuthenticated
                }
                else -> {
                    Log.d(TAG, "Auth state: Authenticated - ${userCore.name}")
                    AuthUiState.Authenticated(userCore)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthUiState.Loading
        )

    fun signOut() {
        Log.d(TAG, "signOut called")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = logOutUseCase()

            result
                .onSuccess {
                    Log.i(TAG, "Sign out successful")
                    analyticsHelper.logEvent(name = "sign_out", params = emptyMap())
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // authUiState는 authRepository.authState를 구독하므로 자동으로 업데이트됨
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
                }
        }
    }
}