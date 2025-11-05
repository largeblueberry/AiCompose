package com.largeblueberry.core_ui

import androidx.compose.runtime.Composable

/**
 * ThemeOption을 화면에 표시할 이름으로 변환하는 기능에 대한 "명세서(인터페이스)".
 * 실제 구현은 다른 모듈(:app)에서 할 것입니다.
 */
interface ThemeDisplayNameProvider {
    @Composable
    fun getDisplayName(themeOption: ThemeOption): String
}