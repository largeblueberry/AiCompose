package com.largeblueberry.feature_sheetmusic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.largeblueberry.resources.R
import com.largeblueberry.feature_sheetmusic.R as ResourcesR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetMusicListScreen(
    sheetMusicList: List<SheetMusic> = emptyList(),
    onSheetMusicClick: (SheetMusic) -> Unit = {},
    onNavigateToRecord: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    /**
     * 리사이클러 뷰로 구현된 악보 리스트 화면
     * 악보가 없을 경우 EmptySheetMusicScreen 호출
     * 악보가 있을 경우 악보 리스트를 보여줌
     * 악보 리스트를 누르면, 악보 상세 화면으로 이동
     */

    if (sheetMusicList.isEmpty()) {
        EmptySheetMusicScreen(
            onNavigateToRecord = onNavigateToRecord,
            onNavigateBack = onNavigateBack
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TopAppBar 추가
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.sheet_music_list_title),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )

            // 악보 리스트
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