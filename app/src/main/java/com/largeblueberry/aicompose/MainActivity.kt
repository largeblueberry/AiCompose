package com.largeblueberry.aicompose

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.largeblueberry.aicompose.nav.AppNavigation
import com.largeblueberry.aicompose.ui.main.MainViewModel
import com.largeblueberry.aicompose.ui.splash.SplashScreen
import com.largeblueberry.core_ui.AppTheme
import com.largeblueberry.domain.repository.LanguageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var languageRepository: LanguageRepository

    // 스플래시 화면 표시 상태
    private var showSplash = mutableStateOf(true)

    // recreate로 인한 재생성인지 구분하기 위한 플래그
    private var isRecreatingForLanguage = false

    private companion object {
        private const val TAG = "MainActivity"
        private const val KEY_IS_RECREATING = "is_recreating_for_language"
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        Log.d(TAG, "attachBaseContext called")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        // savedInstanceState에서 recreate 플래그 복원
        isRecreatingForLanguage = savedInstanceState?.getBoolean(KEY_IS_RECREATING, false) ?: false

        Log.d(TAG, "isRecreatingForLanguage: $isRecreatingForLanguage")

        // 언어 변경으로 인한 재생성이 아닐 때만 스플래시 표시
        if (!isRecreatingForLanguage) {
            // 2초 후 스플래시 화면 숨기기
            lifecycleScope.launch {
                delay(2000)
                showSplash.value = false
                viewModel.checkUserAuthentication()
            }
        } else {
            // 언어 변경으로 인한 재생성이면 즉시 스플래시 숨김
            showSplash.value = false
            viewModel.checkUserAuthentication()
            isRecreatingForLanguage = false // 플래그 리셋
        }

        handleLanguageChanges()

        setContent {
            val themeOption by viewModel.themeOption.collectAsState()
            val isSplashVisible by showSplash

            AppTheme(themeOption = themeOption) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isSplashVisible) {
                        SplashScreen() // 기존 스플래시 컴포저블 사용
                    } else {
                        AppNavigation()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // recreate 플래그를 저장
        outState.putBoolean(KEY_IS_RECREATING, isRecreatingForLanguage)
    }

    private fun handleLanguageChanges() {
        lifecycleScope.launch {
            Log.d(TAG, "handleLanguageChanges: Coroutine launched.")

            languageRepository.language
                .collect { languageCode ->
                    // 현재 Activity의 언어 코드만 추출 (ko-KR -> ko, en-US -> en)
                    val currentLocale = ConfigurationCompat.getLocales(resources.configuration)[0]
                    val currentLanguageCode = currentLocale?.language // ko, en 등만 추출

                    Log.d(TAG, "Language collected: '$languageCode', Current Activity language: '$currentLanguageCode', Full locale: '${currentLocale?.toLanguageTag()}', isRecreatingForLanguage: $isRecreatingForLanguage")

                    // recreate로 인한 재생성이라면 플래그를 리셋하고 처리하지 않음
                    if (isRecreatingForLanguage) {
                        Log.d(TAG, "This is a recreate call, resetting flag and skipping.")
                        isRecreatingForLanguage = false
                        return@collect
                    }

                    // 언어 코드만 비교 (ko vs ko, en vs en)
                    if (currentLanguageCode != languageCode) {
                        Log.d(TAG, "Language mismatch! Current: '$currentLanguageCode', Target: '$languageCode'. Applying and recreating.")
                        isRecreatingForLanguage = true // 플래그 설정

                        // 두 가지 방법 모두 시도
                        try {
                            // 방법 1: AppCompatDelegate 사용
                            val localeList = LocaleListCompat.forLanguageTags(languageCode)
                            AppCompatDelegate.setApplicationLocales(localeList)
                            Log.d(TAG, "AppCompatDelegate.setApplicationLocales called with: $languageCode")

                            // 방법 2: Configuration 직접 변경 (백업)
                            val locale = Locale(languageCode)
                            val config = Configuration(resources.configuration)
                            config.setLocale(locale)
                            resources.updateConfiguration(config, resources.displayMetrics)
                            Log.d(TAG, "Configuration updated directly with: $languageCode")

                        } catch (e: Exception) {
                            Log.e(TAG, "Error setting language: ${e.message}")
                        }

                        // 시스템이 언어를 적용할 시간을 주기 위해 지연
                        delay(200) // 지연 시간을 늘림
                        recreate()
                    } else {
                        Log.d(TAG, "Language matches. Current: '$currentLanguageCode', Target: '$languageCode'. No action needed.")
                    }
                }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
    }
}