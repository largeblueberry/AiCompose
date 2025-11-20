package com.largeblueberry.feature_sheetmusic.ui

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ✅ 해결책 2: ViewModel의 상태를 확인하여 불필요한 데이터 로드를 방지합니다.
    LaunchedEffect(scoreUrl, midiUrl) {
        // scoreUrl과 midiUrl이 유효하고, 아직 데이터가 로드되지 않은 상태일 때만 로드를 요청합니다.
        if (!scoreUrl.isNullOrEmpty() && !midiUrl.isNullOrEmpty()) {
            val currentState = viewModel.uiState.value
            // 이미 Success 상태이고 URL이 현재 URL과 일치하면 다시 로드하지 않습니다.
            if (currentState is SheetMusicUiState.Success && currentState.sheetMusic.scoreUrl == scoreUrl) {
                Log.d("SheetMusicScreen", "✅ 데이터가 이미 로드되어 있으므로, 중복 로드를 생략합니다.")
                return@LaunchedEffect
            }

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
            title = { Text("악보 확인", fontSize = 20.sp, fontWeight = FontWeight.Bold) }, // 제목 변경
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
                // ✅ 해결책 1: 공유 버튼 클릭 시 동작할 로직을 전달합니다.
                onShareClick = { midiToShare ->
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, midiToShare)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "MIDI 공유하기")
                    context.startActivity(shareIntent)
                }
            )
            is SheetMusicUiState.Error -> ErrorContent(
                message = currentState.message,
                onRetry = { viewModel.resetState() }
            )
        }
    }
}

@Composable
private fun IdleContent(onGenerateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎵", fontSize = 80.sp, modifier = Modifier.padding(bottom = 24.dp))
        Text("악보를 생성하거나 선택해주세요", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 32.dp))
        Button(onClick = onGenerateClick, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("샘플 악보 생성하기")
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("악보를 불러오는 중...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SuccessContent(
    sheetMusic: SheetMusic,
    onShareClick: (midiUrl: String) -> Unit // ✅ 해결책 1: 공유 버튼 클릭 이벤트를 받을 람다 함수
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScoreDisplaySection(
            scoreUrl = sheetMusic.scoreUrl,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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

                // ✅ 해결책 1: MIDI 공유 버튼 추가
                // midiUrl이 비어있지 않을 때만 버튼을 보여줍니다.
                if (!sheetMusic.midiUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onShareClick(sheetMusic.midiUrl) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "공유하기",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("MIDI 공유하기")
                    }
                }
            }
        }
    }
}

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
            modifier = Modifier.fillMaxWidth(),
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
        Text("PDF 뷰어 기능은 준비 중입니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
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
