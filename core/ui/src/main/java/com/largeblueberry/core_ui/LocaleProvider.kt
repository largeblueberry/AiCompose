package com.largeblueberry.core_ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

// 현재 언어 코드를 제공하는 CompositionLocal
val LocalLanguageCode = compositionLocalOf { "ko" }

@Composable
fun LocaleProvider(
    languageCode: String,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLanguageCode provides languageCode
    ) {
        content()
    }
}
