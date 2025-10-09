package com.largeblueberry.setting.ui.theme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.setting.ui.theme.domain.ThemeOption
import com.largeblueberry.setting.ui.theme.domain.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class ThemeSettingsUiState(
    val selectedTheme: ThemeOption = ThemeOption.SYSTEM,
    val isDarkTheme: Boolean = false
)

class ThemeViewModel(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    // Repository의 Flow를 관찰
    val uiState: StateFlow<ThemeSettingsUiState> = themeRepository.themeOption
        .map { theme ->
            ThemeSettingsUiState(selectedTheme = theme)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeSettingsUiState() // 초기 값
        )

    fun onThemeSelected(theme: ThemeOption) {
        // 사용자 액션이 들어오면, Repository에 데이터 저장을 요청합니다.
        viewModelScope.launch {
            themeRepository.saveThemeOption(theme)
        }
    }
}