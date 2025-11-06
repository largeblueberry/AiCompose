package com.largeblueberry.setting.theme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.analyticshelper.AnalyticsHelper
import com.largeblueberry.core_ui.ThemeOption
import com.largeblueberry.setting.theme.domain.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    data class UiState(
        val selectedTheme: ThemeOption = ThemeOption.SYSTEM
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            themeRepository.getThemeOption().collect { theme ->
                _uiState.value = _uiState.value.copy(selectedTheme = theme)
            }
        }
    }

    fun onThemeSelected(option: ThemeOption) {

        // 사용자가 테마를 선택했을 때, 분석 이벤트를 보냄.
        analyticsHelper.logEvent(
            name = "theme_select", // 이벤트 이름
            params = mapOf("theme_option" to option.name) // 함께 보낼 데이터
        )

        viewModelScope.launch {
            themeRepository.setThemeOption(option)
        }
    }
}

