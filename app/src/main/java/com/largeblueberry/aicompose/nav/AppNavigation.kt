package com.largeblueberry.aicompose.nav

import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.largeblueberry.aicompose.feature_auth.ui.AccountManageScreen
import com.largeblueberry.aicompose.feature_auth.ui.LoginScreen
import com.largeblueberry.library.ui.screen.LibraryScreen
import com.largeblueberry.aicompose.ui.main.MainScreen
import com.largeblueberry.core_ui.stringResource
import com.largeblueberry.feature_sheetmusic.ui.EmptySheetMusicScreen
import com.largeblueberry.feature_sheetmusic.ui.SheetMusicScreen
import com.largeblueberry.feature_sheetmusic.ui.history.SheetMusicHistoryScreen
import com.largeblueberry.setting.SettingsScreen
import com.largeblueberry.navigation.AppRoutes
import com.largeblueberry.navigation.SettingsNavigationActions
import com.largeblueberry.record.ui.screen.RecordScreenState
import com.largeblueberry.resources.R
import com.largeblueberry.setting.language.LanguageSettingScreen
import com.largeblueberry.setting.theme.ui.ThemeSettingsScreen
import com.largeblueberry.setting.about.AboutUsScreen
import com.largeblueberry.setting.serviceterm.ServiceTermScreen
import com.largeblueberry.setting.serviceterm.TermDetailScreen
import com.largeblueberry.setting.serviceterm.findTermTypeById
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = AppRoutes.MainScreen.route){
        composable(AppRoutes.MainScreen.route) {
            MainScreen(navController = navController)
        }

        composable(AppRoutes.RecordScreen.route) {
            RecordScreenState(navController = navController)
        }

        // 🔥 수정: LibraryScreen의 콜백 처리
        composable(AppRoutes.LibraryScreen.route) {
            LibraryScreen(
                // ✅ scoreUrl과 midiUrl을 모두 받는 콜백으로 변경
                onUploadSuccess = { scoreUrl, midiUrl ->
                    // ✅ URL은 네비게이션 전달 전에 항상 인코딩해야 합니다.
                    val encodedScoreUrl = URLEncoder.encode(scoreUrl, StandardCharsets.UTF_8.toString())
                    val encodedMidiUrl = URLEncoder.encode(midiUrl, StandardCharsets.UTF_8.toString())

                    // ✅ 2. 인코딩된 URL을 포함하여 "상세 주소" 경로를 만듭니다.
                    val routeWithArgs = "${AppRoutes.SheetMusicScreen.route}/$encodedScoreUrl/$encodedMidiUrl"

                    Log.d("AppNavigation", "Navigating to SheetMusicScreen with args: $routeWithArgs")

                    // ✅ 3. 완성된 경로로 내비게이션을 요청합니다.
                    navController.navigate(routeWithArgs)
                },
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 기존 설정 관련 화면들...
        composable(AppRoutes.SettingsScreen.route) {
            SettingsScreen(
                navigationActions = SettingsNavigationActions(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigate(AppRoutes.LoginScreen.route) },
                    onNavigateToLanguage = { navController.navigate(AppRoutes.LanguageSettingScreen.route) },
                    onNavigateToTheme = { navController.navigate(AppRoutes.ThemeSettingScreen.route) },
                    onNavigateToBugReport = { navController.navigate(AppRoutes.BugReportScreen.route) },
                    onNavigateToServiceTerm = { navController.navigate(AppRoutes.ServiceTermScreen.route) },
                    onNavigateToAbout = { navController.navigate(AppRoutes.AboutUsScreen.route) },
                    onNavigateToAccountManage = { navController.navigate(AppRoutes.AccountManageScreen.route) }
                )
            )
        }

        composable(AppRoutes.LoginScreen.route) {
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.AccountManageScreen.route) {
            AccountManageScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.LanguageSettingScreen.route) {
            LanguageSettingScreen(
                navigationActions = SettingsNavigationActions(
                    onNavigateBack = { navController.popBackStack() }
                )
            )
        }

        composable(AppRoutes.ThemeSettingScreen.route) {
            ThemeSettingsScreen(
                navigationActions = SettingsNavigationActions(
                    onNavigateBack = { navController.popBackStack() }
                )
            )
        }

        composable(AppRoutes.ServiceTermScreen.route) {
            ServiceTermScreen(
                navigationActions = SettingsNavigationActions(
                    onNavigateBack = { navController.popBackStack() }
                ),
                onNavigateToDetail = { termType ->
                    val termIdentifier = termType.id
                    navController.navigate("term_detail_route/$termIdentifier")
                }
            )
        }

        composable(
            route = AppRoutes.TermDetailScreen.route,
            arguments = listOf(
                navArgument("termId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val termId = backStackEntry.arguments?.getString("termId")
            val termType = findTermTypeById(termId)

            if (termType != null) {
                TermDetailScreen(
                    termType = termType,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                Text(
                    text = stringResource(id = R.string.error_term_not_found)
                )
            }
        }

        composable(AppRoutes.AboutUsScreen.route) {
            AboutUsScreen(
                navigationActions = SettingsNavigationActions(
                    onNavigateBack = { navController.popBackStack() }
                )
            )
        }

        // 🔥 수정: URL 파라미터를 받는 SheetMusic 화면 정의
        composable(
            route = AppRoutes.SheetMusicScreen.route + "/{scoreUrl}/{midiUrl}",
            arguments = listOf(
                navArgument("scoreUrl") { type = NavType.StringType },
                navArgument("midiUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            Log.d("AppNavigation", "인자 포함된 SheetMusicScreen 호출됨")
            SheetMusicScreen(
                scoreUrl = backStackEntry.arguments?.getString("scoreUrl"),
                midiUrl = backStackEntry.arguments?.getString("midiUrl"),
                onNavigateToRecord = {
                    navController.navigate(AppRoutes.RecordScreen.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 🔥 기존 파라미터 없는 SheetMusic 화면 (빈 화면용)
        composable(AppRoutes.SheetMusicScreen.route) {
            Log.d("AppNavigation", "기본 SheetMusicScreen 호출됨")
            SheetMusicScreen(
                scoreUrl = null,
                midiUrl = null, // midiUrl도 null로 전달
                onNavigateToRecord = {
                    navController.navigate(AppRoutes.RecordScreen.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.SheetMusicHistoryScreen.route) {
            // ✅ 이 컨테이너가 분기 처리를 담당합니다.
            SheetMusicHistoryScreen(
                onScoreClick = { navController.popBackStack() }
            )
        }
    }
}