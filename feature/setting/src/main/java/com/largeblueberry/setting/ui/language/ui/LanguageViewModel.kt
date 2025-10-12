package com.largeblueberry.setting.ui.language.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.setting.ui.language.domain.LanguageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val languageRepository: LanguageRepository
) : ViewModel() {

    // 지원할 언어 목록 정의
    private val availableLanguages = listOf(
        Language("English", "en"),
        Language("한국어", "ko")
    )

    // UI 상태를 StateFlow로 관리
    val uiState: StateFlow<LanguageUiState> = languageRepository.language
        .map { currentLanguageCode ->
            LanguageUiState(
                availableLanguages = availableLanguages,
                selectedLanguageCode = currentLanguageCode
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = LanguageUiState() // 초기 상태
        )

    // 언어 변경을 처리하는 함수
    fun onLanguageSelected(languageCode: String) {
        viewModelScope.launch {
            languageRepository.setLanguage(languageCode)
        }
    }
}