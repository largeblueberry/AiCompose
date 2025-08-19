package com.largeblueberry.aicompose.record.ui.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.largeblueberry.aicompose.library.ui.LibraryActivity
import com.largeblueberry.aicompose.record.ui.RecordViewModel


@Composable
fun RecordScreenState(
    viewModel: RecordViewModel = viewModel()
) {
    val context = LocalContext.current

    // ViewModel의 LiveData를 Compose State로 관찰
    val isRecording by viewModel.isRecording.observeAsState(false)
    val recordingStateText by viewModel.recordingStateText.observeAsState("대기 중")
    val lastSavedFileName by viewModel.lastSavedFileName.observeAsState(null)

    // lastSavedFileName 변경 시 Toast 메시지 표시
    LaunchedEffect(lastSavedFileName) {
        lastSavedFileName?.let { fileName ->
            if (fileName.isNotBlank()) {
                Toast.makeText(context, "녹음 파일이 저장되었습니다: $fileName", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
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
        onRecordToggleClick = {
            if (isRecording) {
                viewModel.stopRecording()
            } else {
                checkPermissionAndStartRecording()
            }
        },
        onViewRecordingsClick = {
            val intent = Intent(context, LibraryActivity::class.java)
            context.startActivity(intent)
        }
    )
}