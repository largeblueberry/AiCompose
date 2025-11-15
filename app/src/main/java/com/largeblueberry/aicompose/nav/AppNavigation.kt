package com.largeblueberry.aicompose.nav

import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.largeblueberry.aicompose.feature_auth.ui.LoginScreen
import com.largeblueberry.library.ui.screen.LibraryScreen
import com.largeblueberry.aicompose.ui.main.MainScreen
import com.largeblueberry.feature_sheetmusic.ui.SheetMusicDetailScreen
import com.largeblueberry.feature_sheetmusic.ui.SheetMusicListScreen
import com.largeblueberry.setting.SettingsScreen
import com.largeblueberry.navigation.AppRoutes
import com.largeblueberry.navigation.SettingsNavigationActions
import com.largeblueberry.record.ui.screen.RecordScreenState
import com.largeblueberry.setting.language.LanguageSettingScreen
import com.largeblueberry.setting.theme.ui.ThemeSettingsScreen
import com.largeblueberry.setting.about.AboutUsScreen

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
                    onNavigateToAbout = { navController.navigate(AppRoutes.AboutUsScreen.route) }
                )
            )
        }

        // 추가된 화면들
        composable(AppRoutes.LoginScreen.route) {
            LoginScreen(

            )
        }

        composable(AppRoutes.LanguageSettingScreen.route) {
            LanguageSettingScreen(

            )
        }

        composable(AppRoutes.ThemeSettingScreen.route) {
            ThemeSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable(AppRoutes.BugReportScreen.route) {
           // BugReportScreen(
           //     onBackClick = { navController.popBackStack() },
           //     onReportSubmitted = { navController.popBackStack() }
           // )
        }

        composable(AppRoutes.ServiceTermScreen.route) {
           // ServiceTermScreen(
            //    onBackClick = { navController.popBackStack() }
          //  )
        }

        composable(AppRoutes.AboutUsScreen.route) {
            AboutUsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable(AppRoutes.SheetMusicListScreen.route) {
            SheetMusicListScreen(
                // 변경: 클릭 시 상세 화면으로 ID와 함께 이동
                onSheetMusicClick = { sheetMusic ->
                    navController.navigate("${AppRoutes.SheetMusicDetailScreen.route}/${sheetMusic.id}")
                },
                // 추가: 빈 화면에서 녹음 화면으로 이동하는 로직 연결
                onNavigateToRecord = {
                    navController.navigate(AppRoutes.RecordScreen.route)
                },
                // 추가: 뒤로가기 처리
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 추가: 악보 상세 화면 라우트 정의
        composable(
            route = "${AppRoutes.SheetMusicDetailScreen.route}/{sheetMusicId}",
            arguments = listOf(navArgument("sheetMusicId") { type = NavType.StringType })
        ) { backStackEntry ->
            // 전달받은 ID를 상세 화면에 넘겨줌
            val sheetMusicId = backStackEntry.arguments?.getString("sheetMusicId")
            SheetMusicDetailScreen(
                sheetMusicId = sheetMusicId,
                // 추가: 뒤로가기 처리
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

    }

}