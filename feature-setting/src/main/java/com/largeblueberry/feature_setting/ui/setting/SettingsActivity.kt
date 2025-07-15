package com.largeblueberry.feature_setting.ui.setting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.largeblueberry.feature_setting.ui.login.LoginScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "settings"
                ) {
                    composable("settings") {
                        SettingsScreen(
                            onNavigateToLogin = {
                                navController.navigate("login")
                            },
                            onNavigateBack = {
                                finish() // MainActivity로 돌아가기
                            }
                        )
                    }

                    composable("login") {
                        LoginScreen(
                            onNavigateBack = {
                                navController.popBackStack() // 설정 화면으로 돌아가기
                            },
                            onNavigateToMain = {
                                finish() // MainActivity로 돌아가기
                            }
                        )
                    }
                }
            }
        }
    }
}