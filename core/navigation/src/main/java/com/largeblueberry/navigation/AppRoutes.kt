package com.largeblueberry.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

    // SheetMusicScreen 라우트 정의를 명확하게 변경
    object SheetMusicScreen : AppRoutes("sheet_music_route") {
        // 1. 경로 템플릿: NavHost에서 이 경로를 인식합니다.
        const val routeWithArgs = "sheet_music_route/{scoreUrl}/{midiUrl}"

        // 2. 아규먼트 이름 상수화
        const val SCORE_URL_ARG = "scoreUrl"
        const val MIDI_URL_ARG = "midiUrl"

        // 3. 실제 내비게이션 시 호출할 함수: URL 인코딩을 포함하여 전체 경로를 생성합니다.
        fun createRoute(scoreUrl: String, midiUrl: String): String {
            val encodedScoreUrl = URLEncoder.encode(scoreUrl, StandardCharsets.UTF_8.toString())
            val encodedMidiUrl = URLEncoder.encode(midiUrl, StandardCharsets.UTF_8.toString())
            return "sheet_music_route/$encodedScoreUrl/$encodedMidiUrl"
        }
    }


    object EmptySheetMusicScreen : AppRoutes("empty_sheet_music_route")
}
