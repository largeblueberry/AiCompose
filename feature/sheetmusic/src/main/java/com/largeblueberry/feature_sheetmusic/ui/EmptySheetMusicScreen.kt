package com.largeblueberry.feature_sheetmusic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.resources.R as ResourcesR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptySheetMusicScreen(
    onNavigateToRecord: () -> Unit = {},
    onNavigateBack: () -> Unit = {} // 파라미터 추가
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TopAppBar 추가
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = ResourcesR.string.sheet_music_list_title),
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

        // 기존 내용
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 큰 이모지
            Text(
                text = "🤔",
                fontSize = 80.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 메인 메시지
            Text(
                text = stringResource(id = ResourcesR.string.empty_sheet_music_main_message),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 서브 메시지
            Text(
                text = stringResource(id = ResourcesR.string.empty_sheet_music_sub_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 액션 버튼 (선택사항)
            Button(
                onClick = onNavigateToRecord,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(id = ResourcesR.string.start_recording_button),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun EmptySheetMusicScreenPreview() {
    MaterialTheme {
        EmptySheetMusicScreen(onNavigateToRecord = {})
    }
}