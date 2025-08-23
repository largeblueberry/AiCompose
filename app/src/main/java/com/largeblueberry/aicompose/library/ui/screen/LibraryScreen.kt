package com.largeblueberry.aicompose.library.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.largeblueberry.aicompose.data.record.remote.model.UploadStatus
import com.largeblueberry.aicompose.library.ui.viemodel.LibraryViewModel
import com.largeblueberry.core_ui.AppPrimaryBlue
import com.largeblueberry.core_ui.AppBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onUploadSuccess: (String) -> Unit,
    navController: NavController,
    onBackClick: () -> Unit // 여기에 추가: 뒤로가기 콜백
) {
    // Context 가져오기 -> toast 메시지 표시를 위해 필요
    val context = LocalContext.current

    //ui 상태를 한 번에 관리
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 삭제 결과 처리
    LaunchedEffect(uiState.deleteResult) {
        uiState.deleteResult?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(context, "파일 삭제 성공.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "파일 삭제 실패.", Toast.LENGTH_SHORT).show()
            }
            viewModel.clearDeleteResult()
        }
    }
    // 업로드 중 메시지 처리
    LaunchedEffect(uiState.showUploadInProgressMessage) {
        if (uiState.showUploadInProgressMessage) {
            Toast.makeText(context, "이미 업로드 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
            viewModel.clearUploadInProgressMessage() // 메시지 표시 후 상태 초기화
        }
    }


    // 업로드 상태 처리
    LaunchedEffect(uiState.uploadState.status) {
        when (uiState.uploadState.status) {
            UploadStatus.SUCCESS -> {
                uiState.uploadState.url?.let { url ->
                    onUploadSuccess(url) // URL 공유 콜백 호출
                    viewModel.clearUploadState()
                    Toast.makeText(context, "업로드 성공!", Toast.LENGTH_SHORT).show()
                }
            }
            UploadStatus.ERROR -> {
                Toast.makeText(context, "서버 업로드에 실패했습니다. 잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                viewModel.clearUploadState()
            }
            else -> {}
        }
    }

    //디자인
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppPrimaryBlue,
            shadowElevation = 4.dp
        ) {
            Box( // Box를 사용하여 아이콘과 텍스트를 배치
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp) // 상하 여백 조정
            ) {
                // 뒤로가기 버튼 (왼쪽 정렬)
                IconButton(
                    onClick = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            onBackClick()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart) // Box의 시작 부분에 정렬
                        .padding(top = 20.dp) // 왼쪽 여백 추가
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // 뒤로가기 아이콘
                        contentDescription = "뒤로가기",
                        tint = Color.White // 흰색으로 설정
                    )
                }

                // 제목 텍스트 (가운데 정렬)
                Text(
                    text = "내 작품",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)// Box의 가운데에 정렬
                        .padding(top = 20.dp)
                )
            }
        }

        if (uiState.isEmpty) {
            EmptyView()
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(), // LazyColumn이 가로 전체를 차지하도록
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp) // 여기에 하단 패딩 추가
            ) {
                items(uiState.audioRecords) { record ->

                    // 현재 재생 중인지 여부를 판단
                    val isPlaying = uiState.currentPlayingRecordId == record.id && uiState.isPlaying

                    // 현재 업로드 중인지 여부를 판단
                    val isUploading = uiState.uploadState.status ==
                            UploadStatus.UPLOADING && uiState.uploadingRecordId == record.id

                    // 현재 레코드가 일시정지 상태인지 여부를 판단
                    val isPaused = uiState.currentPlayingRecordId == record.id && !uiState.isPlaying


                    AudioRecordItem(
                        record = record,
                        isPlaying = isPlaying,         // 현재 재생 중인지 여부
                        isUploading = isUploading,     // 현재 업로드 중인지 여부
                        isPaused = isPaused,           // 현재 이 레코드가 일시정지 상태인지 여부
                        onPlay = { viewModel.playAudio(record) },
                        onPause = { viewModel.pauseAudio() },
                        onStop = { viewModel.stopPlaying() },
                        onDelete = { viewModel.deleteRecord(record) },
                        onUpload = { viewModel.uploadAudioToServer(record.filePath, record.id) },
                        onResume = { viewModel.resumeAudio() }, // resumeAudio 콜백 추가
                        onRename = { viewModel.showRenameDialog(record) } // ViewModel에 다이얼로그 표시 요청
                    )
                }
            }
        }
    }

    // 이름 변경 다이얼로그
    uiState.showRenameDialogForRecord?.let { record ->
        RenameDialog(
            currentName = record.filename,
            onConfirm = { newName ->
                viewModel.renameRecord(record, newName)
            },
            onDismiss = { viewModel.dismissRenameDialog() } // ViewModel에 다이얼로그 닫기 요청
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
                modifier = Modifier
                    .size(64.dp)
                    .background(AppBackground),
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
