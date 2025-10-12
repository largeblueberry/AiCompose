package com.largeblueberry.aicompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.largeblueberry.aicompose.nav.AppNavigation
import com.largeblueberry.aicompose.ui.main.MainViewModel
import com.largeblueberry.core_ui.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val themeOption by viewModel.themeOption.collectAsState()

            // themeOption을 파라미터로 전달하여 테마가 동적으로 변경
            AppTheme(themeOption = themeOption) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    // AppTheme 내부에서 설정된 MaterialTheme의 배경색을 사용합니다.
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