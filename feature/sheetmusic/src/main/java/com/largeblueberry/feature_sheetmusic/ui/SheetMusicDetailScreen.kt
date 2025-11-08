package com.largeblueberry.feature_sheetmusic.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.largeblueberry.resources.R as ResourcesR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// 👇 여기에 파라미터를 추가해 주세요.
fun SheetMusicDetailScreen(
    sheetMusicId: String?,
    onNavigateBack: () -> Unit = {}
) {
    /**
     * 악보 하나의 상세 화면
     * 서버에서 받은 악보를 보여줌.
     */

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TopAppBar 추가
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = ResourcesR.string.sheet_music_detail_title), // 리소스 사용
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) { // 이제 정상 작동
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = ResourcesR.string.navigate_back_content_description) // 기존 리소스 재사용
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // TODO: sheetMusicId를 사용하여 ViewModel에서 악보 상세 정보를 불러와 화면에 표시
        Text(
            // 리소스와 파라미터를 결합하여 사용
            text = "${stringResource(id = ResourcesR.string.sheet_music_detail_screen_message)}\n요청된 ID: $sheetMusicId",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
    }
}
