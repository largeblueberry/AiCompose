package com.largeblueberry.setting.theme.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.domain.model.ThemeOption
import com.largeblueberry.domain.usecase.theme.GetThemeOptionUseCase
import com.largeblueberry.domain.usecase.theme.SetThemeOptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val getThemeOptionUseCase: GetThemeOptionUseCase,
    private val setThemeOptionUseCase: SetThemeOptionUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "ThemeViewModel"
    }

    data class UiState(
        val selectedTheme: ThemeOption = ThemeOption.SYSTEM
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getThemeOptionUseCase().collect { theme ->
                _uiState.value = _uiState.value.copy(selectedTheme = theme)
            }
        }
    }

    fun onThemeSelected(option: ThemeOption) {
        viewModelScope.launch {
            try {
                setThemeOptionUseCase(option)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save theme option to DataStore", e)
            }
        }
    }
}
