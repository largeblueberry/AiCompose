package com.largeblueberry.aicompose.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.aicompose.ui.ThemeOption
import com.largeblueberry.setting.ui.theme.domain.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    themeRepository: ThemeRepository
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
}