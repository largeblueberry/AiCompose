package com.largeblueberry.aicompose

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.largeblueberry.aicompose.nav.AppNavigation
import com.largeblueberry.aicompose.ui.main.MainViewModel
import com.largeblueberry.aicompose.ui.onboarding.OnboardingScreen
import com.largeblueberry.core_ui.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var showSettingsDialog by mutableStateOf(false)

    // 스플래시 화면 표시 상태
    private var showSplash = mutableStateOf(true)

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isRecordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

            if (isRecordAudioGranted) {
                Log.d(TAG, "RECORD_AUDIO permission granted.")
            } else {
                Log.d(TAG, "RECORD_AUDIO permission denied.")
                if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
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
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        lifecycleScope.launch {
            delay(2000.milliseconds)
            showSplash.value = false
        }

        setContent {
            val themeOption by viewModel.themeOption.collectAsState()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

            AppTheme(themeOption = themeOption) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isOnboardingCompleted) {
                        OnboardingScreen(
                            showSplash = showSplash.value,
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

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun completeOnboarding() {
        viewModel.completeOnboarding()
        Log.d(TAG, "Onboarding completed and saved via DataStore")
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
