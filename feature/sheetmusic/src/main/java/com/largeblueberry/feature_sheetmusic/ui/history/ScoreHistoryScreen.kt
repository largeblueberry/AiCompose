package com.largeblueberry.feature_sheetmusic.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.core_ui.component.EareamTopAppBar
import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.feature_sheetmusic.ui.screen.EmptySheetMusicScreen
import com.largeblueberry.resources.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetMusicHistoryScreen(
    viewModel: ScoreHistoryViewModel = hiltViewModel(),
    onScoreClick: (scoreUrl: String, midiUrl: String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToRecord: () -> Unit
) {
    // ✅ ViewModel로부터 악보 목록 상태를 구독
    val scores by viewModel.scores.collectAsState()

    Scaffold(
        topBar = {
            // 1. TopAppBar를 Scaffold의 topBar 파라미터로 이동
            EareamTopAppBar(
                title = stringResource(id = R.string.sheet_music_list_title),
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        if (scores.isEmpty()) {
            EmptySheetMusicScreen(
                modifier = Modifier.padding(paddingValues),
                onNavigateToRecord = onNavigateToRecord
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scores) { score ->
                    ScoreHistoryItem(
                        score = score,
                        onClick = {
                            onScoreClick(score.scoreUrl, score.midiUrl)
                        },
                        // ✅ onDelete 람다에 ViewModel의 deleteScore 함수 연결
                        onDelete = { viewModel.deleteScore(score) }
                    )
                }
            }
        }
    }
}