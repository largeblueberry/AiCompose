package com.largeblueberry.feature_sheetmusic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 악보 데이터 클래스 (임시)
data class SheetMusic(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val createdDate: String
)

@Composable
fun SheetMusicListScreen(
    sheetMusicList: List<SheetMusic> = emptyList(),
    onSheetMusicClick: (SheetMusic) -> Unit = {}
) {
    /**
     * 리사이클러 뷰로 구현된 악보 리스트 화면
     * 악보가 없을 경우 EmptySheetMusicScreen 호출
     * 악보가 있을 경우 악보 리스트를 보여줌
     * 악보 리스트를 누르면, 악보 상세 화면으로 이동
     */

    if (sheetMusicList.isEmpty()) {
        EmptySheetMusicScreen()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sheetMusicList) { sheetMusic ->
                SheetMusicItem(
                    sheetMusic = sheetMusic,
                    onClick = { onSheetMusicClick(sheetMusic) }
                )
            }
        }
    }
}

@Composable
private fun SheetMusicItem(
    sheetMusic: SheetMusic,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이모지 사용 (가장 확실함)
            Text(
                text = "🎼",
                fontSize = 24.sp,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp)
            )

            // 악보 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = sheetMusic.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = sheetMusic.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⏱️ ${sheetMusic.duration}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "📅 ${sheetMusic.createdDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SheetMusicListScreenPreview() {
    val sampleData = listOf(
        SheetMusic("1", "Spring Day", "BTS", "3:47", "2024.11.06"),
        SheetMusic("2", "Dynamite", "BTS", "3:19", "2024.11.05"),
        SheetMusic("3", "Butter", "BTS", "2:44", "2024.11.04")
    )

    MaterialTheme {
        SheetMusicListScreen(
            sheetMusicList = sampleData,
            onSheetMusicClick = { /* Preview에서는 동작 안함 */ }
        )
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun SheetMusicListScreenEmptyPreview() {
    MaterialTheme {
        SheetMusicListScreen(
            sheetMusicList = emptyList()
        )
    }
}