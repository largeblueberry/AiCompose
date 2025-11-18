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
    object AboutUsScreen : AppRoutes("about_route")
    object TermDetailScreen : AppRoutes("term_detail_route/{termId}")
    object AccountManageScreen : AppRoutes("account_manage_route")
    object SheetMusicScreen : AppRoutes("sheet_music_route")
    object EmptySheetMusicScreen : AppRoutes("empty_sheet_music_route")

}