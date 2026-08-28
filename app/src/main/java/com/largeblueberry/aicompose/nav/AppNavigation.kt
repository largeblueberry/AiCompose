package com.largeblueberry.aicompose.nav

import androidx.compose.ui.platform.LocalContext
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

        composable(AppRoutes.LibraryScreen.route) {
            LibraryScreen(
                onUploadSuccess = { scoreUrl, midiUrl ->
                    val encodedScoreUrl = URLEncoder.encode(scoreUrl, StandardCharsets.UTF_8.toString())
                    val encodedMidiUrl = URLEncoder.encode(midiUrl, StandardCharsets.UTF_8.toString())
                    val routeWithArgs = "${AppRoutes.SheetMusicScreen.route}/$encodedScoreUrl/$encodedMidiUrl"

                    Log.d("AppNavigation", "Navigating to SheetMusicScreen with args: $routeWithArgs")
                    navController.navigate(routeWithArgs)
                },
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }

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
            SheetMusicHistoryScreen(
                onScoreClick = { scoreUrl, midiUrl ->
                    val encodedScoreUrl = URLEncoder.encode(scoreUrl, StandardCharsets.UTF_8.toString())
                    val encodedMidiUrl = URLEncoder.encode(midiUrl, StandardCharsets.UTF_8.toString())

                    val routeWithArgs = "${AppRoutes.SheetMusicScreen.route}/$encodedScoreUrl/$encodedMidiUrl"

                    Log.d("AppNavigation", "Navigating from History to SheetMusicScreen with args: $routeWithArgs")

                    navController.navigate(routeWithArgs)
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRecord = { navController.navigate(AppRoutes.RecordScreen.route) }
            )
        }
    }
}