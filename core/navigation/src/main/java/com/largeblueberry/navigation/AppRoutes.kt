package com.largeblueberry.navigation

import android.net.Uri

sealed class AppRoutes(val route: String) {
    object MainScreen : AppRoutes("main_route")
    object RecordScreen : AppRoutes("record_route")
    object LibraryScreen : AppRoutes("library_route")
    object SettingsScreen : AppRoutes("settings_route")
    object ThemeSettingScreen : AppRoutes("theme_route")
    object LanguageSettingScreen : AppRoutes("language_route")
    object BugReportScreen : AppRoutes("bug_report_route")
    object ServiceTermScreen : AppRoutes("service_term_route")
    object AboutUsScreen : AppRoutes("about_route")
    object TermDetailScreen : AppRoutes("term_detail_route/{termId}") {
        const val TERM_ID_ARG = "termId"

        fun createRoute(termId: String): String = "term_detail_route/$termId"
    }
    object SheetMusicHistoryScreen : AppRoutes("sheet_music_history_route")

    /**
     * 악보 상세. 악보 id만 넘기고, 화면(ViewModel)이 저장소에서 읽는다.
     * 예전에는 서버 URL 두 개를 인코딩해 경로에 실었다(docs/troubleshooting.md C-2).
     */
    object SheetMusicScreen : AppRoutes("sheet_music_route/{scoreId}") {
        const val SCORE_ID_ARG = "scoreId"

        fun createRoute(scoreId: String): String = "sheet_music_route/${Uri.encode(scoreId)}"
    }

    object EmptySheetMusicScreen : AppRoutes("empty_sheet_music_route")
}
