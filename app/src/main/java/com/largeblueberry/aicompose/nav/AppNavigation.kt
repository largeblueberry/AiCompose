package com.largeblueberry.aicompose.nav

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.largeblueberry.library.ui.screen.LibraryScreen
import com.largeblueberry.aicompose.ui.main.MainScreen
import androidx.compose.ui.res.stringResource
import com.largeblueberry.feature_sheetmusic.ui.SheetMusicScreen
import com.largeblueberry.feature_sheetmusic.ui.history.SheetMusicHistoryScreen
import com.largeblueberry.setting.SettingsScreen
import com.largeblueberry.navigation.AppRoutes
import com.largeblueberry.record.ui.screen.RecordScreenState
import com.largeblueberry.resources.R
import com.largeblueberry.setting.language.LanguageSettingScreen
import com.largeblueberry.setting.theme.ui.ThemeSettingsScreen
import com.largeblueberry.setting.about.AboutUsScreen
import com.largeblueberry.setting.serviceterm.ServiceTermScreen
import com.largeblueberry.setting.serviceterm.TermDetailScreen
import com.largeblueberry.setting.serviceterm.findTermTypeById

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

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
                    val routeWithArgs = AppRoutes.SheetMusicScreen.createRoute(scoreUrl, midiUrl)

                    Log.d("AppNavigation", "Navigating to SheetMusicScreen with args: $routeWithArgs")
                    navController.navigate(routeWithArgs)
                },
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.SettingsScreen.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLanguage = { navController.navigate(AppRoutes.LanguageSettingScreen.route) },
                onNavigateToTheme = { navController.navigate(AppRoutes.ThemeSettingScreen.route) },
                onNavigateToBugReport = { navController.navigate(AppRoutes.BugReportScreen.route) },
                onNavigateToServiceTerm = { navController.navigate(AppRoutes.ServiceTermScreen.route) },
                onNavigateToAbout = { navController.navigate(AppRoutes.AboutUsScreen.route) }
            )
        }

        composable(AppRoutes.LanguageSettingScreen.route) {
            LanguageSettingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.ThemeSettingScreen.route) {
            ThemeSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.ServiceTermScreen.route) {
            ServiceTermScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { termType ->
                    navController.navigate(AppRoutes.TermDetailScreen.createRoute(termType.id))
                }
            )
        }

        composable(
            route = AppRoutes.TermDetailScreen.route,
            arguments = listOf(
                navArgument(AppRoutes.TermDetailScreen.TERM_ID_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val termId = backStackEntry.arguments?.getString(AppRoutes.TermDetailScreen.TERM_ID_ARG)
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
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.SheetMusicScreen.routeWithArgs,
            arguments = listOf(
                navArgument(AppRoutes.SheetMusicScreen.SCORE_URL_ARG) { type = NavType.StringType },
                navArgument(AppRoutes.SheetMusicScreen.MIDI_URL_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            Log.d("AppNavigation", "인자 포함된 SheetMusicScreen 호출됨")
            SheetMusicScreen(
                scoreUrl = backStackEntry.arguments?.getString(AppRoutes.SheetMusicScreen.SCORE_URL_ARG),
                midiUrl = backStackEntry.arguments?.getString(AppRoutes.SheetMusicScreen.MIDI_URL_ARG),
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
                    val routeWithArgs = AppRoutes.SheetMusicScreen.createRoute(scoreUrl, midiUrl)

                    Log.d("AppNavigation", "Navigating from History to SheetMusicScreen with args: $routeWithArgs")

                    navController.navigate(routeWithArgs)
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRecord = { navController.navigate(AppRoutes.RecordScreen.route) }
            )
        }
    }
}