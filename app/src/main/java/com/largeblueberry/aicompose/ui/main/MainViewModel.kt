package com.largeblueberry.aicompose.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.auth.repository.AuthRepository
import com.largeblueberry.auth.usecase.SignInAnonymouslyUseCase
import com.largeblueberry.core_ui.ThemeOption
import com.largeblueberry.domain.repository.LanguageRepository
import com.largeblueberry.setting.theme.domain.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Locale

@HiltViewModel
class MainViewModel @Inject constructor(
    themeRepository: ThemeRepository,
    languageRepository: LanguageRepository,
    // [추가] 인증 관련 의존성 주입
    private val authRepository: AuthRepository,
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase
) : ViewModel() {



    // Repository의 getThemeOption() Flow를 구독하여
    // UI가 사용하기 좋은 StateFlow<ThemeOption>으로 변환합
    val themeOption: StateFlow<ThemeOption> = themeRepository.getThemeOption()
        .stateIn(
            scope = viewModelScope,
            // 앱이 활성화되어 있을 때만 구독을 시작하고, 5초 후 중지 (메모리 효율)
            started = SharingStarted.WhileSubscribed(5_000),
            // 초기값 설정
            initialValue = ThemeOption.SYSTEM
        )

    val languageCode: StateFlow<String> = languageRepository.language
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Locale.getDefault().language // 초기값은 시스템 언어로 설정
        )


    /**
     * 앱 시작 시 사용자 인증 상태를 확인하고,
     * 로그인 되어 있지 않으면 익명 로그인을 시도합니다.
     */
    fun checkUserAuthentication() {
        viewModelScope.launch {
            // 1. 현재 유저 정보 가져오기 (로그인 또는 익명 상태)
            val currentUser = authRepository.getCurrentUser()

            // 2. 유저 정보가 없다면 (완전 비로그인 상태) 익명 로그인 시도
            if (currentUser == null) {
                signInAnonymouslyUseCase()
                    .onSuccess {
                        Log.d("MainViewModel", "익명 인증에 성공했습니다.")
                        // 이제부터 API 호출 시 토큰이 자동으로 포함됩니다.
                    }
                    .onFailure { exception ->
                        Log.e("MainViewModel", "익명 인증 실패: ${exception.message}")
                        // 네트워크 오류 등의 예외 처리
                    }
            } else {
                Log.d("MainViewModel", "이미 인증된 사용자입니다. (ID: ${currentUser.id})")
            }
        }
    }
}