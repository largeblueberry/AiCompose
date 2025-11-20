package com.largeblueberry.feature_sheetmusic.ui

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.feature_sheetmusic.ui.screen.RemoteScoreViewer
import com.largeblueberry.feature_sheetmusic.ui.state.SheetMusicUiState

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

    LaunchedEffect(scoreUrl, midiUrl) {
        if (!scoreUrl.isNullOrEmpty() && !midiUrl.isNullOrEmpty()) {
            Log.d("SheetMusicScreen", "🚀 업로드된 파일 로드 시작")
            Log.d("SheetMusicScreen", "  - Score URL: $scoreUrl")
            Log.d("SheetMusicScreen", "  - MIDI URL: $midiUrl")
            viewModel.loadUploadedFiles(midiUrl = midiUrl, scoreUrl = scoreUrl)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("악보 생성", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                }
            },
            actions = {
                IconButton(onClick = onNavigateToRecord) {
                    Icon(Icons.Default.Add, "새 악보 생성")
                }
            }
        )

        when (val currentState = uiState) {
            is SheetMusicUiState.Idle -> IdleContent { viewModel.generateSheetMusic("sample") }
            is SheetMusicUiState.Loading -> LoadingContent()
            is SheetMusicUiState.Success -> SuccessContent(
                sheetMusic = currentState.sheetMusic,
                onReset = { viewModel.resetState() }
            )
            is SheetMusicUiState.Error -> ErrorContent(
                message = currentState.message,
                onRetry = { viewModel.resetState() }
            )
        }
    }
}

// ... IdleContent, LoadingContent 는 기존과 동일 ...
@Composable
private fun IdleContent(onGenerateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎵", fontSize = 80.sp, modifier = Modifier.padding(bottom = 24.dp))
        Text("악보를 생성해보세요", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 32.dp))
        Button(onClick = onGenerateClick, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("악보 생성하기")
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("악보를 생성하는 중...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


// 🎵 SuccessContent 레이아웃 구조 개선
@Composable
private fun SuccessContent(
    sheetMusic: SheetMusic,
    onReset: () -> Unit
) {
    // 🔥 Column 전체를 스크롤 가능하도록 변경
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 스크롤 추가
            .padding(16.dp)
    ) {
        // 🎵 악보 표시 영역
        ScoreDisplaySection(
            scoreUrl = sheetMusic.scoreUrl,
            modifier = Modifier
                .fillMaxWidth()
                // 🔥 weight(1f) 제거하여 콘텐츠 크기에 맞춰 높이가 조절되도록 함
                .padding(bottom = 16.dp)
        )

        // 📋 악보 정보 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "제목: ${sheetMusic.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                sheetMusic.composer?.let {
                    Text("작곡가: $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
                }
                sheetMusic.createdAt?.let {
                    Text("생성 시간: $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
                }
                sheetMusic.duration?.let {
                    Text("재생 시간: ${it}초", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
                }
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

        // 🔄 다시 생성하기 버튼 (Column 내부로 이동)
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

// 🎵 ScoreDisplaySection (변경 없음, 그러나 내부의 RemoteScoreViewer가 동적으로 높이를 조절하게 됨)
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
            modifier = Modifier.fillMaxWidth(), // 높이는 자식(웹뷰)에 의해 결정됨
            contentAlignment = Alignment.Center
        ) {
            when {
                scoreUrl.isNullOrEmpty() -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 32.dp)) {
                        Text("🎼", fontSize = 48.sp, modifier = Modifier.padding(bottom = 8.dp))
                        Text("악보를 불러올 수 없습니다", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                scoreUrl.endsWith(".pdf", ignoreCase = true) -> {
                    PdfScoreViewer(pdfUrl = scoreUrl, modifier = Modifier.fillMaxSize())
                }
                else -> {
                    RemoteScoreViewer(
                        scoreUrl = scoreUrl,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ... PdfScoreViewer, ErrorContent 는 기존과 동일 ...
@Composable
private fun PdfScoreViewer(pdfUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📄", fontSize = 48.sp, modifier = Modifier.padding(bottom = 8.dp))
        Text("PDF 악보", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Text("PDF 뷰어 구현 예정", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, pdfUrl.toUri())
            context.startActivity(intent)
        }) {
            Text("외부 앱으로 열기")
        }
    }
}

@Composable
fun ErrorContent(modifier: Modifier = Modifier, message: String, onRetry: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️", fontSize = 80.sp, modifier = Modifier.padding(bottom = 24.dp))
        Text("오류가 발생했습니다", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 32.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("다시 시도")
        }
    }
}
