package com.largeblueberry.feature_sheetmusic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.largeblueberry.core_ui.component.EareamTopAppBar
import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.feature_sheetmusic.ui.screen.ScoreWebView
import com.largeblueberry.feature_sheetmusic.ui.state.SheetMusicUiState
import com.largeblueberry.feature_sheetmusic.ui.util.MidiPlayer
import com.largeblueberry.feature_sheetmusic.ui.util.ScoreSharing
import com.largeblueberry.feature_sheetmusic.ui.util.formatCreatedAt
import kotlinx.coroutines.launch
import com.largeblueberry.resources.R

/**
 * 악보 상세. 상태를 받아 그리기만 한다. 불러오기는 ViewModel이 스스로 한다.
 */
@Composable
fun SheetMusicScreen(
    onNavigateBack: () -> Unit,
    viewModel: SheetMusicViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SheetMusicContent(uiState = uiState, onNavigateBack = onNavigateBack, onRetry = viewModel::retry)
}

@Composable
internal fun SheetMusicContent(
    uiState: SheetMusicUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EareamTopAppBar(
                title = (uiState as? SheetMusicUiState.Success)?.sheetMusic?.title
                    ?: stringResource(R.string.sheet_music_detail_title),
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        val modifier = Modifier
            .padding(padding)
            .fillMaxSize()
        when (uiState) {
            SheetMusicUiState.Loading -> Box(modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            SheetMusicUiState.NotFound -> MessageContent(
                message = stringResource(R.string.sheetMusicNotFound),
                modifier = modifier,
            )
            SheetMusicUiState.Error -> MessageContent(
                message = stringResource(R.string.sheetMusicLoadError),
                modifier = modifier,
                onRetry = onRetry,
            )
            is SheetMusicUiState.Success -> Column(
                modifier = modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScoreCard(uiState.musicXml)
                ScoreActions(uiState.sheetMusic, snackbarHostState)
                ScoreInfoCard(uiState.sheetMusic)
            }
        }
    }
}

/** 악보 그림. 테마와 상관없이 흰 바탕에 검은 악보(OSMD 기본)로 그린다. */
@Composable
private fun ScoreCard(musicXml: String) {
    // 그리기 실패는 이 화면 안에서만 의미 있는 상태라 화면이 들고 있는다. 다른 악보면 다시 시도한다.
    var renderFailed by remember(musicXml) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        if (LocalInspectionMode.current) {
            // IDE 미리보기에서는 WebView를 만들 수 없다
            Box(Modifier.fillMaxWidth().height(160.dp))
        } else if (renderFailed) {
            Text(
                text = stringResource(R.string.sheetMusicRenderError),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            )
        } else {
            ScoreWebView(musicXml = musicXml, onRenderError = { renderFailed = true })
        }
    }
}

/**
 * 재생·공유. 재생은 이 화면이 보이는 동안만 한다(화면을 떠나거나 앱이 가려지면 멈춘다).
 * 재생기는 화면 생명주기에 묶인 자원이라 ViewModel이 아니라 화면이 만들고 정리한다.
 */
@Composable
private fun ScoreActions(sheetMusic: SheetMusic, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val player = remember { MidiPlayer() }
    var isPlaying by remember { mutableStateOf(false) }
    val playError = stringResource(R.string.sheetMusicPlayError)
    val shareMidiTitle = stringResource(R.string.sheetMusicShareMidi)
    val shareXmlTitle = stringResource(R.string.sheetMusicShareXml)

    DisposableEffect(player) { onDispose { player.stop() } }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        player.stop()
        isPlaying = false
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(
            modifier = Modifier.weight(1f),
            onClick = {
                if (isPlaying) {
                    player.stop()
                    isPlaying = false
                } else {
                    isPlaying = player.play(sheetMusic.midiPath, onComplete = { isPlaying = false })
                    if (!isPlaying) scope.launch { snackbarHostState.showSnackbar(playError) }
                }
            }
        ) {
            Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(if (isPlaying) R.string.sheetMusicStop else R.string.sheetMusicPlay))
        }
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { ScoreSharing.share(context, sheetMusic.midiPath, ScoreSharing.MIME_MIDI, shareMidiTitle) }
        ) {
            // 글자는 형식 이름만 보이고, 화면 읽기 프로그램에는 "MIDI 공유"로 읽힌다
            Icon(Icons.Default.Share, contentDescription = shareMidiTitle, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text("MIDI")
        }
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { ScoreSharing.share(context, sheetMusic.musicXmlPath, ScoreSharing.MIME_MUSIC_XML, shareXmlTitle) }
        ) {
            Icon(Icons.Default.Share, contentDescription = shareXmlTitle, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text("MusicXML")
        }
    }
}

@Composable
private fun ScoreInfoCard(sheetMusic: SheetMusic) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.sheetMusicInfo, sheetMusic.bpm, sheetMusic.keyName, sheetMusic.noteCount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.sheetMusicCreatedAt, formatCreatedAt(sheetMusic.createdAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageContent(message: String, modifier: Modifier, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}

private val previewSheet = SheetMusic(
    id = "score_1",
    title = "20261123_120000",
    recordId = 1,
    createdAt = 1_763_870_400_000,
    bpm = 100,
    beatsPerBar = 4,
    keyName = "C major",
    noteCount = 19,
    musicXmlPath = "",
    midiPath = "",
)

@Preview(showBackground = true)
@Composable
private fun SheetMusicSuccessPreview() {
    MaterialTheme { SheetMusicContent(SheetMusicUiState.Success(previewSheet, ""), {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun SheetMusicErrorPreview() {
    MaterialTheme { SheetMusicContent(SheetMusicUiState.Error, {}, {}) }
}
