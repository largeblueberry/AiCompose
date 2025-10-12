package com.largeblueberry.setting.ui.theme.domain

sealed class ThemeOption(val key: String) {
    object SYSTEM : ThemeOption("system")
    object LIGHT : ThemeOption("light")
    object DARK : ThemeOption("dark")

    fun toDisplayName(): String = when(this) {
        LIGHT -> "라이트"
        DARK -> "다크"
        SYSTEM -> "시스템 설정"
    }
}