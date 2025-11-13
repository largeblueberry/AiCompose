package com.largeblueberry.navigation

sealed class AppRoutes(val route: String) {
    object MainScreen : AppRoutes("main_route")
    object RecordScreen : AppRoutes("record_route")
    object LibraryScreen : AppRoutes("library_route")
    object SettingsScreen : AppRoutes("settings_route")
    object LoginScreen : AppRoutes("login_route")
    object ThemeSettingScreen : AppRoutes("theme_route")
    object LanguageSettingScreen : AppRoutes("language_route")
    object BugReportScreen : AppRoutes("bug_report_route")
    object ServiceTermScreen : AppRoutes("service_term_route")
    object AboutScreen : AppRoutes("about_route")
    object SheetMusicListScreen : AppRoutes("sheet_music_list_route")
    object SheetMusicDetailScreen : AppRoutes("sheet_music_detail_route/{sheetMusicId}")
}