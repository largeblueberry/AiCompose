package com.largeblueberry.aicompose.nav

import androidx.compose.ui.platform.LocalContext
import android.content.Intent
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
            LibraryScreen(onUploadSuccess = { url ->

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, url)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "업로드 하기"))
            },
                navController = navController, // 메인 NavController를 전달
                onBackClick = { navController.popBackStack() } // 뒤로 가기 시 NavController 사용
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

        // 추가된 화면들
        composable(AppRoutes.LoginScreen.route) {
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.AccountManageScreen.route) {
            // AccountManageScreen 컴포넌트를 여기에 추가
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
                    // TermType의 id를 사용하여 상세 화면으로 이동
                    val termIdentifier = termType.id

                    // AppRoutes.TermDetailScreen.route = "term_detail_route/{termId}"
                    navController.navigate("term_detail_route/$termIdentifier")
                }
            )
        }

        composable(
            route = AppRoutes.TermDetailScreen.route, // "term_detail_route/{termId}"
            arguments = listOf(
                navArgument("termId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val termId = backStackEntry.arguments?.getString("termId")

            // termId를 이용해 해당 TermType 데이터를 찾습니다.
            val termType = findTermTypeById(termId)

            if (termType != null) {
                TermDetailScreen(
                    termType = termType,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // 오류 처리 (예: 404 화면 또는 이전 화면으로 돌아가기)
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


        composable(AppRoutes.SheetMusicScreen.route) {
            SheetMusicScreen(
                onNavigateToRecord = {
                    // Record 화면으로 이동
                    navController.navigate(AppRoutes.RecordScreen.route)
                },
                onNavigateBack = {
                    // 이전 화면으로 돌아가기
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.EmptySheetMusicScreen.route) {
            EmptySheetMusicScreen(
                onNavigateToRecord = {
                    // Record 화면으로 이동
                    navController.navigate(AppRoutes.RecordScreen.route)
                },
                onNavigateBack = {
                    // 이전 화면으로 돌아가기
                    navController.popBackStack()
                }
            )
        }
    }

}