package com.largeblueberry.aicompose.database.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.largeblueberry.aicompose.record.ui.AudioPlayer
import com.largeblueberry.aicompose.record.database.AudioRecordEntity
import com.largeblueberry.aicompose.retrofit.data.UploadStatus

class AudioRecordDataActivity : ComponentActivity() {

    private val viewModel: AudioRecordViewModel by viewModels {
        AudioRecordViewModelFactory(applicationContext)
    }

    private val audioPlayer = AudioPlayer()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AudioRecordScreen(
                    viewModel = viewModel,
                    audioPlayer = audioPlayer,
                    onShare = { url -> shareUrl(url) }
                )
            }
        }
    }

    private fun shareUrl(url: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "공유하기"))
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
    }
}

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

    var showRenameDialog by remember { mutableStateOf<AudioRecordEntity?>(null) }

    // 삭제 결과 처리
    LaunchedEffect(deleteResult) {
        deleteResult?.let { result ->
            if (result.isSuccess) {
                // 성공 처리
            } else {
                // 에러 처리
            }
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
                        isUploading = uploadState.status == UploadStatus.UPLOADING,
                        onPlay = { audioPlayer.play(record.filePath) },
                        onDelete = { viewModel.deleteRecord(record) },
                        onShare = { viewModel.uploadAudioToServer(record.filePath) },
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecordItem(
    record: AudioRecordEntity,
    isUploading: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 재생 버튼
            IconButton(onClick = onPlay) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "재생"
                )
            }

            // 파일 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = record.filename,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatFileSize(record.fileSize),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = record.createdAt.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 액션 버튼들
            Row {
                // 이름 변경
                IconButton(onClick = onRename) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "이름 변경"
                    )
                }

                // 공유
                IconButton(
                    onClick = onShare,
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "공유"
                        )
                    }
                }

                // 삭제
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ✅ 함수가 올바른 위치에 있음
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes <= 0 -> "알 수 없음"
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
        else -> "${bytes / (1024 * 1024 * 1024)}GB"
    }
}

// ✅ RenameDialog가 올바른 위치에 있음
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("파일 이름 변경") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("새 이름") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newName.isNotBlank() && newName != currentName) {
                        onConfirm(newName.trim())
                    }
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}