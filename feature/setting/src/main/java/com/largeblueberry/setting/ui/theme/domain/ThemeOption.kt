package com.largeblueberry.setting.ui.theme.domain

sealed class ThemeOption(val key: String) {
    object SYSTEM : ThemeOption("system")
    object LIGHT : ThemeOption("light")
    object DARK : ThemeOption("dark")

    companion object {
        fun fromKey(key: String?): ThemeOption {
            return when (key) {
                LIGHT.key -> LIGHT
                DARK.key -> DARK
                else -> SYSTEM
            }
        }
    }
}