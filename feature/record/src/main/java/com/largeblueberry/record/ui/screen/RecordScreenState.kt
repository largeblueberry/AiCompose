package com.largeblueberry.record.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.largeblueberry.navigation.AppRoutes
import com.largeblueberry.record.ui.RecordViewModel
import com.largeblueberry.resources.R
import kotlinx.coroutines.launch


@Composable
fun RecordScreenState(
    navController: NavController,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // ViewModel의 StateFlow를 Compose State로 관찰
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingStateText by viewModel.recordingStateText.collectAsStateWithLifecycle()
    val lastSavedFileName by viewModel.lastSavedFileName.collectAsStateWithLifecycle()

    // lastSavedFileName 변경 시 Snackbar 메시지 표시
    LaunchedEffect(lastSavedFileName) {
        lastSavedFileName?.let { fileName ->
            if (fileName.isNotBlank()) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.recordSavedMSG, fileName),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // RECORD_AUDIO 권한 요청을 위한 런처
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startRecording()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.recordPermission),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // 권한 확인 및 녹음 시작 함수
    val checkPermissionAndStartRecording: () -> Unit = {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.startRecording()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    RecordScreenUi(
        isRecording = isRecording,
        recordingStateText = recordingStateText,
        snackbarHostState = snackbarHostState,
        onRecordToggleClick = {
            if (isRecording) {
                viewModel.stopRecording()
            } else {
                checkPermissionAndStartRecording()
            }
        },
        onViewRecordingsClick = {
            navController.navigate(AppRoutes.LibraryScreen.route)
        }
    )
}