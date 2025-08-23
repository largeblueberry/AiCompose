package com.largeblueberry.aicompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.largeblueberry.aicompose.nav.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme,
                typography = MaterialTheme.typography,
                shapes = MaterialTheme.shapes
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * Surface는 Material Design 컴포넌트의 기본 배경을 제공하는 역할
 * 이 MainActivity는 Jetpack Compose 앱의 진입점입니다.
 * 앱이 시작될 때 호출되며, 앱의 최상위 UI 구조와 테마를 설정하는 역할을 합니다.
 * setContent 블록 내에서 Composable 함수들을 호출하여 화면을 구성합니다.
 */