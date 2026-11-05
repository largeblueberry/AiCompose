package com.largeblueberry.aicompose

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.largeblueberry.aicompose.nav.AppNavigation
import com.largeblueberry.aicompose.ui.main.MainViewModel
import com.largeblueberry.aicompose.ui.onboarding.OnboardingScreen
import com.largeblueberry.aicompose.ui.splash.SplashScreen
import com.largeblueberry.core_ui.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var showSettingsDialog by mutableStateOf(false)

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isRecordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            val isAudioReadGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.READ_MEDIA_AUDIO] ?: true
            } else {
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: true
            }

            if (isRecordAudioGranted && isAudioReadGranted) {
                Log.d(TAG, "Required permissions granted.")
            } else {
                Log.d(TAG, "Permissions denied.")
                val rationalePermission = if (!isRecordAudioGranted) {
                    Manifest.permission.RECORD_AUDIO
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                if (!shouldShowRequestPermissionRationale(rationalePermission)) {
                    Log.d(TAG, "Permission might be permanently denied. Showing settings dialog.")
                    showSettingsDialog = true
                }
            }
        }

    private companion object {
        private const val TAG = "MainActivity"
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        Log.d(TAG, "attachBaseContext called")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        setContent {
            val themeOption by viewModel.themeOption.collectAsStateWithLifecycle()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1200.milliseconds) // 브랜드 스플래시 UI (로고+타이틀) 1.2초 표출 후 부드럽게 페이드아웃
                showSplash = false
            }

            AppTheme(themeOption = themeOption) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!isOnboardingCompleted) {
                            OnboardingScreen(
                                showSettingsDialog = showSettingsDialog,
                                onDismissSettingsDialog = { showSettingsDialog = false },
                                onGoToSettingsClick = {
                                    openAppSettings()
                                    showSettingsDialog = false
                                },
                                onPermissionRequest = {
                                    requestPermissions()
                                },
                                onComplete = {
                                    viewModel.completeOnboarding()
                                    Log.d(TAG, "Onboarding completed and saved via DataStore")
                                }
                            )
                        } else {
                            AppNavigation()
                        }

                        AnimatedVisibility(
                            visible = showSplash,
                            enter = fadeIn(animationSpec = tween(400)),
                            exit = fadeOut(animationSpec = tween(400))
                        ) {
                            SplashScreen()
                        }
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
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
