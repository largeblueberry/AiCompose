package com.largeblueberry.feature_sheetmusic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.resources.R as ResourcesR

@Composable
fun EmptySheetMusicScreen() {
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
            text = "아직 악보가 없네요",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 서브 메시지
        Text(
            text = "녹음 먼저 시작해볼까요?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 액션 버튼 (선택사항)
        Button(
            onClick = { /* TODO: 녹음 화면으로 이동 */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "🎤 녹음 시작하기",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptySheetMusicScreenPreview() {
    MaterialTheme {
        EmptySheetMusicScreen()
    }
}