package com.largeblueberry.aicompose.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.domain.model.ThemeOption
import com.largeblueberry.domain.repository.ThemeRepository
import com.largeblueberry.domain.usecase.setting.GetLanguageCodeUseCase
import com.largeblueberry.domain.usecase.setting.GetOnboardingCompletedUseCase
import com.largeblueberry.domain.usecase.setting.SetOnboardingCompletedUseCase
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
    getLanguageCodeUseCase: GetLanguageCodeUseCase,
    getOnboardingCompletedUseCase: GetOnboardingCompletedUseCase,
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase
) : ViewModel() {

    val themeOption: StateFlow<ThemeOption> = themeRepository.getThemeOption()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeOption.SYSTEM
        )

    val languageCode: StateFlow<String> = getLanguageCodeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Locale.getDefault().language
        )

    val isOnboardingCompleted: StateFlow<Boolean> = getOnboardingCompletedUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun completeOnboarding() {
        viewModelScope.launch {
            setOnboardingCompletedUseCase(true)
        }
    }
}
