package com.largeblueberry.navigation

import android.net.Uri

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

    // 🔥 수정: SheetMusicScreen에 URL 파라미터 지원 추가
    object SheetMusicScreen : AppRoutes("sheet_music_route") {
        const val routeWithArgs = "sheet_music_route/{scoreUrl}"

        fun createRoute(scoreUrl: String): String {
            return "sheet_music_route/${Uri.encode(scoreUrl)}"
        }
    }


    object EmptySheetMusicScreen : AppRoutes("empty_sheet_music_route")
}
