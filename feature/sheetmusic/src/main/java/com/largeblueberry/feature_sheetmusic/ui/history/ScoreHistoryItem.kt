package com.largeblueberry.feature_sheetmusic.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.largeblueberry.core_ui.customColors
import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.resources.R

@Composable
fun ScoreHistoryItem(
    score: SheetMusic,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = score.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "저장일: ${score.createdAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.customColors.cardViewSubText
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.deleteDescription),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// SheetMusic 데이터 클래스 변경사항을 반영한 Preview 코드
@Preview(showBackground = true)
@Composable
fun ScoreHistoryItemPreview() {
    // YourAppTheme 같은 테마로 감싸주면 더 정확한 미리보기가 가능합니다.
    ScoreHistoryItem(
        score = SheetMusic(
            id = "preview_id_1",
            title = "미리보기 제목",
            composer = "작곡가",
            scoreUrl = "https://example.com/score.pdf",
            midiUrl = "https://example.com/score.mid",
            createdAt = "2025-11-20",
            duration = 180,
            key = "C Major",
            tempo = 120
        ),
        onClick = {},
        onDelete = {}
    )
}
