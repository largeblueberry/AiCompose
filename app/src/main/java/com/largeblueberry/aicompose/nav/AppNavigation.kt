package com.largeblueberry.aicompose.nav

import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.largeblueberry.aicompose.library.ui.screen.LibraryScreen
import com.largeblueberry.aicompose.record.ui.screen.RecordScreenState
import com.largeblueberry.aicompose.ui.main.MainScreen
import com.largeblueberry.feature_setting.ui.setting.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = AppRoutes.MainScreen.route){
        composable(AppRoutes.MainScreen.route) {
            MainScreen(navController = navController)
        }

        composable(AppRoutes.RecordScreen.route) {
            RecordScreenState()
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
            SettingsScreen() // 설정 화면 컴포저블 함수 호출
        }
    }


}