package com.largeblueberry.setting.viewmodel

import android.util.Log // Log 임포트
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.analyticshelper.AnalyticsHelper
import com.largeblueberry.auth.model.AuthUiState
import com.largeblueberry.auth.model.LoginUiState
import com.largeblueberry.setting.domain.usecase.LogoutUseCaseImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val logOutUseCase: LogoutUseCaseImpl,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    // Logcat 필터링을 위한 TAG 추가
    private companion object {
        private const val TAG = "SettingViewModel"
    }

    private val _uiState = MutableStateFlow(LoginUiState())

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // 🚪 로그아웃
    fun signOut() {
        // 로그아웃 시도 로그
        Log.d(TAG, "signOut called")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = logOutUseCase()

            result
                .onSuccess {
                    // 로그아웃 성공 로그 및 이벤트
                    Log.i(TAG, "Sign out successful")
                    analyticsHelper.logEvent(name = "sign_out", params = emptyMap())

                    _authUiState.value = AuthUiState.NotAuthenticated
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { exception ->
                    // 로그아웃 실패 로그 및 이벤트
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
}