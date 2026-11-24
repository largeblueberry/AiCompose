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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.largeblueberry.domain.model.RecorderState
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
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 문자열은 LocalContext가 아니라 컴포지션(stringResource, LocalResources)에서 읽는다.
    // LocalContext로 읽으면 설정(언어)이 바뀌어도 옛 값이 남는다(troubleshooting C-13).
    val resources = LocalResources.current
    val maxLengthMessage = stringResource(R.string.recordMaxLength)
    val permissionMessage = stringResource(R.string.recordPermission)

    LaunchedEffect(uiState.savedFileName) {
        val name = uiState.savedFileName ?: return@LaunchedEffect
        // 파일 이름은 이 시점에야 정해지므로 포맷 문자열은 LocalResources로 읽는다
        val saved = resources.getString(R.string.recordSavedMSG, name)
        val message = if (uiState.stoppedAtLimit) maxLengthMessage + "\n" + saved else saved
        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        viewModel.consumeSavedMessage()
    }

    // 화면이 안 보이게 되면 녹음을 버린다(백그라운드에서는 마이크가 막혀 무음만 녹음된다)
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onScreenStopped() }

    // 카운트인 박마다 진동. 녹음 중에는 진동하지 않는다(모터 소리가 마이크로 들어가 음으로 잡힐 수 있다).
    val countIn = uiState.recorder as? RecorderState.CountIn
    LaunchedEffect(countIn?.beatsLeft) {
        if (countIn != null) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startRecording()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = permissionMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    RecordScreenUi(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRecordToggleClick = {
            when {
                uiState.isActive -> viewModel.stopRecording()
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED -> viewModel.startRecording()
                else -> requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        onBpmChange = viewModel::changeBpm,
        onViewRecordingsClick = {
            navController.navigate(AppRoutes.LibraryScreen.route)
        }
    )
}
