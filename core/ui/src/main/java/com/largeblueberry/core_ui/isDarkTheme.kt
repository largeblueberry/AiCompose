package com.largeblueberry.core_ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable


@Composable
fun isDarkTheme(

): Boolean{
    val themeMode = LocalThemeMode.current

    return when(themeMode){
        is ThemeMode.Dark -> true
        is ThemeMode.Light -> false
        is ThemeMode.System -> isSystemInDarkTheme()
    }
}