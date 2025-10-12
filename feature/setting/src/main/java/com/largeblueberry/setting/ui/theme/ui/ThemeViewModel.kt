package com.largeblueberry.setting.ui.theme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.setting.ui.theme.domain.ThemeOption
import com.largeblueberry.setting.ui.theme.domain.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
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
        viewModelScope.launch {
            themeRepository.setThemeOption(option)
        }
    }
}

