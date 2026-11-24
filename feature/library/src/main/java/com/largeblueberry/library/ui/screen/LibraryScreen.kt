package com.largeblueberry.library.ui.screen

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.largeblueberry.core_ui.customColors
import com.largeblueberry.library.ui.viemodel.LibraryViewModel
import com.largeblueberry.resources.R as ResourcesR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    /** 채보가 끝난 항목의 "악보 보기". 끝나도 자동으로 옮기지 않는다(사용자가 다른 화면에 있을 수 있다). */
    onOpenScore: (scoreId: String) -> Unit,
    navController: NavController,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }

    suspend fun showSnackBar(snackBarMessage: String){
        snackBarHostState.showSnackbar(
            message = snackBarMessage,
            duration = SnackbarDuration.Short
        )
    }

    // 문자열은 컴포지션에서 읽는다. LocalContext로 읽으면 언어를 바꿔도 옛 값이 남는다(troubleshooting C-13).
    val deleteSuccessMessage = stringResource(ResourcesR.string.fileDeleteSuccess)
    val deleteErrorMessage = stringResource(ResourcesR.string.fileDeleteError)

    // 채보 상태는 항목에 계속 보이므로 일회성 안내(스낵바)는 삭제 결과만 쓴다
    LaunchedEffect(uiState.deleteResult) {
        uiState.deleteResult?.let { result ->
            val message = if (result.isSuccess) deleteSuccessMessage else deleteErrorMessage
            showSnackBar(message)
            viewModel.clearDeleteResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {

                    IconButton(
                        onClick = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                onBackClick()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(top = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(ResourcesR.string.backButtonContentDescription),
                            tint = MaterialTheme.customColors.appBlack
                        )
                    }

                    Text(
                        text = stringResource(ResourcesR.string.myLibrary),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.customColors.appBlack,
                        modifier = Modifier
                            .align(Alignment.Center)
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
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.audioRecords) { record ->

                        val isPlaying = uiState.currentPlayingRecordId == record.id && uiState.isPlaying

                        val isPaused = uiState.currentPlayingRecordId == record.id && !uiState.isPlaying

                        AudioRecordItem(
                            record = record,
                            isPlaying = isPlaying,
                            transcription = uiState.transcriptionOf(record.id),
                            isPaused = isPaused,
                            onPlay = { viewModel.playAudio(record) },
                            onPause = { viewModel.pauseAudio() },
                            onStop = { viewModel.stopPlaying() },
                            onDelete = { viewModel.deleteRecord(record) },
                            onTranscribe = { viewModel.transcribe(record) },
                            onOpenScore = onOpenScore,
                            onResume = { viewModel.resumeAudio() },
                            onRename = { viewModel.showRenameDialog(record) }
                        )
                    }
                }
            }
        }

    }

    uiState.showRenameDialogForRecord?.let { record ->
        RenameDialog(
            currentName = record.filename,
            onConfirm = { newName ->
                viewModel.renameRecord(record, newName)
            },
            onDismiss = { viewModel.dismissRenameDialog() }
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
                    .background(MaterialTheme.colorScheme.background),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(ResourcesR.string.emptyLibrary),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
