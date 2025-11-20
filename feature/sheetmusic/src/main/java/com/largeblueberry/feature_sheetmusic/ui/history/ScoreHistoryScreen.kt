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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.feature_sheetmusic.ui.EmptySheetMusicScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetMusicHistoryScreen(
    viewModel: ScoreHistoryViewModel = hiltViewModel(),
    onScoreClick: (String) -> Unit // 악보 클릭 시 상세 페이지로 이동하기 위한 콜백
) {
    // ✅ ViewModel로부터 악보 목록 상태를 구독
    val scores by viewModel.scores.collectAsState()

    Scaffold(
    ) { paddingValues ->
        if (scores.isEmpty()) {
            EmptySheetMusicScreen()
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scores) { score ->
                    ScoreHistoryItem(score = score, onClick = { onScoreClick(score.id) })
                }
            }
        }
    }
}

@Composable
fun ScoreHistoryItem(
    score: SheetMusic,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = score.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "저장일: ${score.createdAt}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
