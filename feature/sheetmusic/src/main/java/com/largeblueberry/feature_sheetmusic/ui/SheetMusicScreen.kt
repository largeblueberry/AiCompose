package com.largeblueberry.feature_sheetmusic.ui

import android.util.Log
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
import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.feature_sheetmusic.ui.state.SheetMusicUiState
import com.largeblueberry.feature_sheetmusic.ui.screen.RemoteScoreViewer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetMusicScreen(
    scoreUrl: String? = null,
    midiUrl: String? = null,
    onNavigateToRecord: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: SheetMusicViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 🔥 scoreUrl이 변경될 때만 실행되도록 개선
    LaunchedEffect(scoreUrl, midiUrl) {
        if (!scoreUrl.isNullOrEmpty() && !midiUrl.isNullOrEmpty()) {
            Log.d("SheetMusicScreen", "🚀 업로드된 파일 로드 시작")
            Log.d("SheetMusicScreen", "  - Score URL: $scoreUrl")
            Log.d("SheetMusicScreen", "  - MIDI URL: $midiUrl")
            // 🔥 수정: 두 개의 URL을 모두 전달하는 올바른 함수를 호출합니다.
            viewModel.loadUploadedFiles(midiUrl = midiUrl, scoreUrl = scoreUrl)
        }
    }

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

        // 🔥 when 표현식 개선 (불필요한 캐스팅 제거)
        when (val currentState = uiState) {
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
                    sheetMusic = currentState.sheetMusic, // 🔥 스마트 캐스팅 활용
                    onReset = { viewModel.resetState() }
                )
            }
            is SheetMusicUiState.Error -> {
                ErrorContent(
                    message = currentState.message, // 🔥 스마트 캐스팅 활용
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

// 🎵 SuccessContent 완전히 새로 작성
@Composable
private fun SuccessContent(
    sheetMusic: SheetMusic,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🎵 악보 표시 영역 (새로 추가)
        ScoreDisplaySection(
            scoreUrl = sheetMusic.scoreUrl,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 남은 공간을 모두 사용
                .padding(bottom = 16.dp)
        )

        // 📋 악보 정보 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "제목: ${sheetMusic.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                sheetMusic.composer?.let { composer ->
                    Text(
                        text = "작곡가: $composer",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                sheetMusic.createdAt?.let { createdAt ->
                    Text(
                        text = "생성 시간: $createdAt",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                sheetMusic.duration?.let { duration ->
                    Text(
                        text = "재생 시간: ${duration}초",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // URL 정보 표시
                Text(
                    text = "악보 URL: ${if (sheetMusic.scoreUrl.isNullOrEmpty()) "없음" else "사용 가능"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (sheetMusic.scoreUrl.isNullOrEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "MIDI URL: ${if (sheetMusic.midiUrl.isNullOrEmpty()) "없음" else "사용 가능"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (sheetMusic.midiUrl.isNullOrEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // 🔄 다시 생성하기 버튼
    Button(
        onClick = onReset,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text("다시 생성하기")

    }
}

// 🎵 새로 추가: 악보 표시 컴포넌트
@Composable
private fun ScoreDisplaySection(
    scoreUrl: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                scoreUrl.isNullOrEmpty() -> {
                    // URL이 없는 경우
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎼",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "악보를 불러올 수 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    // 웹뷰 파일인 경우
                    RemoteScoreViewer(
                        scoreUrl = scoreUrl, // scoreUrl 전달
                        modifier = Modifier.fillMaxSize() // Modifier도 동일하게 적용
                    )
                }
            }
        }
    }
}

// 오류 발생 시 재시도 UI를 표시하는 Composable
@Composable
fun ErrorContent(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("재시도")
            }
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