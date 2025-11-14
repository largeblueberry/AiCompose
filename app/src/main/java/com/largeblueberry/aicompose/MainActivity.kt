package com.largeblueberry.aicompose

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.largeblueberry.aicompose.nav.AppNavigation
import com.largeblueberry.aicompose.ui.main.MainViewModel
import com.largeblueberry.aicompose.ui.onboarding.OnboardingScreen
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

    private var isOnboardingCompleted by mutableStateOf(false)

    @Inject
    lateinit var languageRepository: LanguageRepository

    // 스플래시 화면 표시 상태
    private var showSplash = mutableStateOf(true)

    // recreate로 인한 재생성인지 구분하기 위한 플래그
    private var isRecreatingForLanguage = false

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // RECORD_AUDIO 권한이 허용되었는지 확인
            val isRecordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

            if (isRecordAudioGranted) {
                Log.d(TAG, "RECORD_AUDIO permission granted.")
                // 권한이 허용되었을 때 필요한 작업 수행
            } else {
                Log.d(TAG, "RECORD_AUDIO permission denied.")
                // 권한이 거부되었을 때 사용자에게 알림 (필요한 경우)
            }
            // 다른 권한들에 대한 처리도 추가할 수 있습니다.
        }

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

            AppTheme(themeOption = themeOption) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isOnboardingCompleted) {
                        // 온보딩 화면 (Splash + 온보딩 페이저)
                        OnboardingScreen(
                            onPermissionRequest = {
                                requestPermissions()
                            },
                            onComplete = {
                                // 온보딩 완료 후 메인 앱으로
                                completeOnboarding()
                            }
                        )
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

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // RECORD_AUDIO 권한 확인 및 요청 목록에 추가
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun completeOnboarding() {
        // TODO: SharedPreferences 등에 온보딩 완료 상태를 저장하는 로직 추가 예정
        isOnboardingCompleted = true
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