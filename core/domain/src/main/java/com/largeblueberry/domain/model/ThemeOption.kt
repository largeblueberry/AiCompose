package com.largeblueberry.domain.model

enum class ThemeOption(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromKey(key: String?): ThemeOption {
            return entries.find { it.key == key } ?: SYSTEM
        }
    }
}
