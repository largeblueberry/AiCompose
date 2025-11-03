package com.largeblueberry.setting.language.ui

// 언어 정보를 담는 데이터 클래스
data class Language(
    val name: String, // UI에 표시될 이름 (예: "한국어")
    val code: String  // Locale을 위한 코드 (예: "ko")
)