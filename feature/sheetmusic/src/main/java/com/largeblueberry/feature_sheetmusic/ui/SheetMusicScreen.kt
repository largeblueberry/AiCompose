package com.largeblueberry.feature_sheetmusic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.largeblueberry.feature_sheetmusic.ui.state.SheetMusicUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetMusicScreen(
    onNavigateToRecord: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: SheetMusicViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TopAppBar
        TopAppBar(
            title = {
                Text(
                    text = "악보 생성",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로 가기"
                    )
                }
            },
            actions = {
                IconButton(onClick = onNavigateToRecord) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "새 악보 생성"
                    )
                }
            }
        )

        // Content based on actual ViewModel states
        when (uiState) {
            is SheetMusicUiState.Idle -> {
                IdleContent(
                    onGenerateClick = {
                        // TODO: 실제 requestBody 전달
                        viewModel.generateSheetMusic(requestBody = "sample")
                    }
                )
            }
            is SheetMusicUiState.Loading -> {
                LoadingContent()
            }
            is SheetMusicUiState.Success -> {
                SuccessContent(
                    sheetMusic = (uiState as SheetMusicUiState.Success).sheetMusic,
                    onReset = { viewModel.resetState() }
                )
            }
            is SheetMusicUiState.Error -> {
                ErrorContent(
                    message = (uiState as SheetMusicUiState.Error).message,
                    onRetry = { viewModel.resetState() }
                )
            }
        }
    }
}

@Composable
private fun IdleContent(
    onGenerateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎵",
            fontSize = 80.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "악보를 생성해보세요",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onGenerateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("악보 생성하기")
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "악보를 생성하는 중...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SuccessContent(
    sheetMusic: Any, // 실제 SheetMusic 타입으로 변경 필요
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✅",
            fontSize = 80.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "악보 생성 완료!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // TODO: 실제 SheetMusic 정보 표시
        Text(
            text = sheetMusic.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("다시 생성하기")
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = 80.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "오류가 발생했습니다",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("다시 시도")
        }
    }
}