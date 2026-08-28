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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.largeblueberry.core_ui.customColors
import com.largeblueberry.library.ui.viemodel.LibraryViewModel
import com.largeblueberry.network.model.request.UploadStatus
import com.largeblueberry.resources.R as ResourcesR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onUploadSuccess: (scoreUrl: String, midiUrl: String) -> Unit,
    navController: NavController,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }

    suspend fun showSnackBar(snackBarMessage: String){
        snackBarHostState.showSnackbar(
            message = snackBarMessage,
            duration = SnackbarDuration.Short
        )
    }

    /**
     * 삭제 결과 처리, 업로드 중 메시지 처리, 업로드 상태 처리
     */
    LaunchedEffect(uiState.deleteResult) {
        uiState.deleteResult?.let { result ->
            val message = if (result.isSuccess) {
                context.getString(ResourcesR.string.fileDeleteSuccess)
            } else {
                context.getString(ResourcesR.string.fileDeleteError)
            }
            showSnackBar(message)
            viewModel.clearDeleteResult()
        }
    }

    LaunchedEffect(uiState.showUploadInProgressMessage) {
        if (uiState.showUploadInProgressMessage) {
            showSnackBar(context.getString(ResourcesR.string.serverUploading))
            viewModel.clearUploadInProgressMessage()
        }
    }

    LaunchedEffect(uiState.uploadState.status) {
        when (uiState.uploadState.status) {
            UploadStatus.SUCCESS -> {
                val scoreUrl = uiState.uploadState.scoreUrl
                val midiUrl = uiState.uploadState.midiUrl

                if (!scoreUrl.isNullOrEmpty() && !midiUrl.isNullOrEmpty()) {
                    onUploadSuccess(scoreUrl, midiUrl)
                    viewModel.clearUploadState()
                    showSnackBar(context.getString(ResourcesR.string.serverUploadSuccess))
                } else {
                    showSnackBar(context.getString(ResourcesR.string.serverUploadError))
                    viewModel.clearUploadState()
                }
            }
            UploadStatus.ERROR -> {
                showSnackBar(context.getString(ResourcesR.string.serverUploadError))
                viewModel.clearUploadState()
            }
            else -> {}
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

                    uiState.currentUploads?.let { current ->
                        uiState.maxUploads?.let { max ->
                            Text(
                                text = "($current/$max)",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.customColors.appBlack,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(top = 20.dp)
                                    .padding(end = 10.dp)
                            )
                        }
                    }

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

                        val isUploading = uiState.uploadState.status ==
                                UploadStatus.UPLOADING && uiState.uploadingRecordId == record.id

                        val isPaused = uiState.currentPlayingRecordId == record.id && !uiState.isPlaying

                        AudioRecordItem(
                            record = record,
                            isPlaying = isPlaying,
                            isUploading = isUploading,
                            isPaused = isPaused,
                            onPlay = { viewModel.playAudio(record) },
                            onPause = { viewModel.pauseAudio() },
                            onStop = { viewModel.stopPlaying() },
                            onDelete = { viewModel.deleteRecord(record) },
                            onUpload = { viewModel.uploadAudioToServer(record.filePath, record.id) },
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
