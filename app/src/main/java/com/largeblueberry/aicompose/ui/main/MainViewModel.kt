package com.largeblueberry.aicompose.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.core_ui.ThemeOption
import com.largeblueberry.domain.repository.LanguageRepository
import com.largeblueberry.setting.theme.domain.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import java.util.Locale

@HiltViewModel
class MainViewModel @Inject constructor(
    themeRepository: ThemeRepository,
    languageRepository: LanguageRepository
) : ViewModel() {

    val themeOption: StateFlow<ThemeOption> = themeRepository.getThemeOption()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeOption.SYSTEM
        )

    val languageCode: StateFlow<String> = languageRepository.language
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Locale.getDefault().language
        )
}
