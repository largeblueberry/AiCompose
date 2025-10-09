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


// --- ViewModel Layer (제공해주신 코드) ---

data class ThemeSettingsUiState(
    val selectedTheme: ThemeOption = ThemeOption.SYSTEM
    // isDarkTheme 필드는 앱 전체 테마 적용 시 필요하므로 여기서는 직접 사용하지 않습니다.
)

class ThemeViewModel(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val uiState: StateFlow<ThemeSettingsUiState> = themeRepository.themeOption
        .map { theme ->
            ThemeSettingsUiState(selectedTheme = theme)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeSettingsUiState()
        )

    fun onThemeSelected(theme: ThemeOption) {
        viewModelScope.launch {
            themeRepository.saveThemeOption(theme)
        }
    }
}
