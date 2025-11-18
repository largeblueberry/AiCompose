package com.largeblueberry.feature_sheetmusic.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import com.largeblueberry.resources.R as ResourcesR
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.feature_sheetmusic.domain.SheetMusicDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetMusicDetailScreen(
    sheetMusicId: String,
    onNavigateBack: () -> Unit = {},
    onPlayMidi: (String) -> Unit = {},
    viewModel: SheetMusicDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 화면 진입시 데이터 로드
    LaunchedEffect(sheetMusicId) {
        viewModel.loadSheetMusic(sheetMusicId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TopAppBar
        TopAppBar(
            title = {
                Text(
                    text = when {
                        uiState.sheetMusic != null -> uiState.sheetMusic!!.title
                        uiState.isLoading -> "로딩 중..."
                        else -> stringResource(id = ResourcesR.string.sheet_music_detail_title)
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = ResourcesR.string.navigate_back_content_description)
                    )
                }
            },
            actions = {
                // MIDI 재생 버튼
                if (uiState.canPlayMidi) {
                    IconButton(
                        onClick = {
                            viewModel.onMidiPlayClicked()
                            uiState.sheetMusic?.midiUrl?.let { midiUrl ->
                                onPlayMidi(midiUrl)
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "MIDI 재생",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        // 메인 컨텐츠
        when {
            uiState.isLoading -> {
                LoadingContent()
            }
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error!!,
                    onRetry = { viewModel.retry(sheetMusicId) },
                    onDismissError = { viewModel.clearError() }
                )
            }
            uiState.sheetMusic != null -> {
                SheetMusicContent(
                    sheetMusic = uiState.sheetMusic!!,
                    onScoreViewClicked = { viewModel.onScoreViewClicked() },
                    modifier = Modifier.fillMaxSize()
                )
            }
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
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "📄",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "악보를 불러오는 중...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "❌",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onDismissError) {
                    Text("확인")
                }

                Button(onClick = onRetry) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("다시 시도")
                }
            }
        }
    }
}

@Composable
private fun SheetMusicContent(
    sheetMusic: SheetMusicDetail,
    onScoreViewClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 악보 정보 카드
        SheetMusicInfoCard(sheetMusic = sheetMusic)

        Spacer(modifier = Modifier.height(20.dp))

        // 악보 표시 카드
        SheetMusicViewCard(
            sheetMusic = sheetMusic,
            onScoreViewClicked = onScoreViewClicked
        )
    }
}

@Composable
private fun SheetMusicInfoCard(sheetMusic: SheetMusicDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 제목
            Text(
                text = sheetMusic.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 아티스트
            Text(
                text = sheetMusic.artist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 상태 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // MIDI 상태
                StatusChip(
                    label = "MIDI",
                    isAvailable = sheetMusic.hasMidi,
                    icon = "🎵"
                )

                // 악보 상태
                StatusChip(
                    label = "악보",
                    isAvailable = sheetMusic.hasScore,
                    icon = "🎼"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 상세 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    icon = "⏱️",
                    label = "재생시간",
                    value = sheetMusic.duration
                )

                InfoItem(
                    icon = "📅",
                    label = "생성일",
                    value = sheetMusic.createdDate
                )
            }
        }
    }
}

@Composable
private fun SheetMusicViewCard(
    sheetMusic: SheetMusicDetail,
    onScoreViewClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎼 악보",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (sheetMusic.hasScore) {
                    TextButton(onClick = onScoreViewClicked) {
                        Text("전체화면")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sheetMusic.hasScore && !sheetMusic.scoreUrl.isNullOrBlank()) {
                // 실제 악보 표시
                SheetMusicWebView(
                    url = sheetMusic.scoreUrl,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 악보 없음 표시
                NoScoreContent(hasMidi = sheetMusic.hasMidi)
            }
        }
    }
}

@Composable
private fun NoScoreContent(hasMidi: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (hasMidi) "🎵" else "❌",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasMidi) {
                    "악보 생성 실패"
                } else {
                    "악보 없음"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (hasMidi) {
                    "MIDI 파일은 재생 가능합니다"
                } else {
                    "악보 데이터가 없습니다"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    isAvailable: Boolean,
    icon: String
) {
    Surface(
        color = if (isAvailable) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                fontSize = 14.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (isAvailable) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (isAvailable) {
                Text(
                    text = "✓",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: String,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SheetMusicWebView(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Log.d("SheetMusicWebView", "🌐 Loading URL: $url")

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        Log.d("SheetMusicWebView", "🔄 Page started loading: $url")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("SheetMusicWebView", "✅ Page finished loading: $url")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        Log.e("SheetMusicWebView", "❌ WebView error: $errorCode - $description for URL: $failingUrl")
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                }
            }
        },
        update = { webView ->
            Log.d("SheetMusicWebView", "🔄 Updating WebView with URL: $url")
            webView.loadUrl(url)
        },
        modifier = modifier
    )
}