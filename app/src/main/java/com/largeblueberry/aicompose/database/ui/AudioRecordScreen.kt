package com.largeblueberry.aicompose.database.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.largeblueberry.aicompose.database.ui.viemodel.AudioRecordViewModel
import com.largeblueberry.aicompose.record.ui.AudioPlayer
import com.largeblueberry.aicompose.record.database.AudioRecordEntity
import com.largeblueberry.aicompose.retrofit.data.UploadStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecordScreen(
    viewModel: AudioRecordViewModel,
    audioPlayer: AudioPlayer,
    onShare: (String) -> Unit
) {
    val audioRecords by viewModel.audioRecords.collectAsStateWithLifecycle()
    val isEmpty by viewModel.isEmpty.collectAsStateWithLifecycle()
    val deleteResult by viewModel.deleteResult.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()
    val uploadingRecordId by viewModel.uploadingRecordId.collectAsStateWithLifecycle()

    var showRenameDialog by remember { mutableStateOf<AudioRecordEntity?>(null) }

    // 삭제 결과 처리
    LaunchedEffect(deleteResult) {
        deleteResult?.let { result ->
            // 성공/실패 처리(토스트 등)
            viewModel.clearDeleteResult()
        }
    }
    // 업로드 상태 처리
    LaunchedEffect(uploadState.status) {
        when (uploadState.status) {
            UploadStatus.SUCCESS -> {
                uploadState.url?.let { url ->
                    onShare(url)
                    viewModel.clearUploadState()
                }
            }
            UploadStatus.ERROR -> {
                // 에러 메시지 표시
                viewModel.clearUploadState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "녹음 기록",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isEmpty) {
            EmptyView()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(audioRecords) { record ->
                    AudioRecordItem(
                        record = record,
                        isUploading = uploadState.status == UploadStatus.UPLOADING && uploadingRecordId == record.id,
                        onPlay = { audioPlayer.play(record.filePath) },
                        onDelete = { viewModel.deleteRecord(record) },
                        onShare = { viewModel.uploadAudioToServer(record.filePath, record.id) },
                        onRename = { showRenameDialog = record }
                    )
                }
            }
        }
    }

    // 이름 변경 다이얼로그
    showRenameDialog?.let { record ->
        RenameDialog(
            currentName = record.filename,
            onConfirm = { newName ->
                viewModel.renameRecord(record, newName)
                showRenameDialog = null
            },
            onDismiss = { showRenameDialog = null }
        )
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "녹음된 파일이 없습니다",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
