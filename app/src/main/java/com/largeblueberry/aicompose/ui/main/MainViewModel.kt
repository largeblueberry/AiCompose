package com.largeblueberry.aicompose.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.domain.model.ThemeOption
import com.largeblueberry.domain.usecase.setting.GetOnboardingCompletedUseCase
import com.largeblueberry.domain.usecase.setting.SetOnboardingCompletedUseCase
import com.largeblueberry.domain.usecase.theme.GetThemeOptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getThemeOptionUseCase: GetThemeOptionUseCase,
    getOnboardingCompletedUseCase: GetOnboardingCompletedUseCase,
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase
) : ViewModel() {

    val themeOption: StateFlow<ThemeOption> = getThemeOptionUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeOption.SYSTEM
        )

    val isOnboardingCompleted: StateFlow<Boolean> = getOnboardingCompletedUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun completeOnboarding() {
        viewModelScope.launch {
            try {
                setOnboardingCompletedUseCase(true)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to save onboarding completion state to DataStore", e)
            }
        }
    }
}
