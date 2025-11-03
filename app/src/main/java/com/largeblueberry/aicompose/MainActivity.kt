package com.largeblueberry.aicompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import com.largeblueberry.aicompose.nav.AppNavigation
import com.largeblueberry.aicompose.ui.main.MainViewModel
import com.largeblueberry.aicompose.ui.AppTheme
import com.largeblueberry.setting.language.ui.LanguageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeOption by viewModel.themeOption.collectAsState()
            val languageUiState by languageViewModel.uiState.collectAsState()

            LaunchedEffect(languageUiState.selectedLanguageCode) {
                // ⛔️ 수정 전: if (languageUiState.selectedLanguageCode.isNotBlank())
                // ✅ 수정 후: isNullOrBlank()를 사용하여 null 안정성을 확보합니다.
                if (!languageUiState.selectedLanguageCode.isNullOrBlank()) {
                    // 이 블록 안에서는 selectedLanguageCode가 null이 아님이 보장됩니다.
                    val appLocale = LocaleListCompat.forLanguageTags(languageUiState.selectedLanguageCode)
                    if (AppCompatDelegate.getApplicationLocales() != appLocale) {
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                }
            }

            AppTheme(themeOption = themeOption) {
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