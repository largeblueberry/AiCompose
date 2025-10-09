package com.largeblueberry.core_ui

sealed class ThemeMode{
    object Light : ThemeMode()
    object Dark : ThemeMode()
    object System : ThemeMode()
}