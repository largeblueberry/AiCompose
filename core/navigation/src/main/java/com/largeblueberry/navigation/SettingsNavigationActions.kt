package com.largeblueberry.navigation

data class SettingsNavigationActions(
    val onNavigateBack: () -> Unit = {},
    val onNavigateToLogin: () -> Unit = {},
    val onNavigateToLanguage: () -> Unit = {},
    val onNavigateToTheme: () -> Unit = {},
    val onNavigateToBugReport: () -> Unit = {},
    val onNavigateToServiceTerm: () -> Unit = {},
    val onNavigateToAbout: () -> Unit = {},
    val onNavigateToAccountManage: () -> Unit = {}
)