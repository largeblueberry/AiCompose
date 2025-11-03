package com.largeblueberry.setting.language.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.setting.language.domain.LanguageRepository
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
        // 2. 앱에 언어 변경 즉시 적용 (추가된 코드)
        // 안드로이드 시스템에 어떤 언어를 사용할지 알려줍니다.
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

}