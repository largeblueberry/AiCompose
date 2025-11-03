package com.largeblueberry.setting.language.ui

// UI 상태를 나타내는 데이터 클래스
data class LanguageUiState(
    val availableLanguages: List<Language> = emptyList(),
    val selectedLanguageCode: String? = null
)