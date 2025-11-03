package com.largeblueberry.library.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.largeblueberry.core_ui.CustomColors
import com.largeblueberry.core_ui.LocalCustomColors
import com.largeblueberry.library.domainLayer.model.LibraryModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.largeblueberry.resources.R as ResourceR
// 더 깔끔한 접근을 위해 Theme 객체를 만들어 사용하면 좋습니다.
object Theme {
    val customColors: CustomColors
        @Composable
        get() = LocalCustomColors.current
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecordItem(
    record: LibraryModel,
    isPlaying: Boolean,
    isUploading: Boolean,
    isPaused: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit,
    onUpload: () -> Unit,
    onRename: () -> Unit,
    onResume: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        // 1. 카드 배경색을 커스텀 테마 색상으로 변경
        colors = CardDefaults.cardColors(containerColor = Theme.customColors.cardViewBackground)
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
                    text = record.filename,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.clickable { onRename() },
                    // 2. 카드뷰의 메인 텍스트 색상을 커스텀 테마 색상으로 변경
                    color = Theme.customColors.cardViewMainText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.duration,
                    style = MaterialTheme.typography.bodyMedium,
                    // 3. 카드뷰의 서브 텍스트 색상을 커스텀 테마 색상으로 변경
                    color = Theme.customColors.cardViewSubText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(record.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    // 3. 카드뷰의 서브 텍스트 색상을 커스텀 테마 색상으로 변경
                    color = Theme.customColors.cardViewSubText
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // 재생/일시정지/정지 버튼 로직 (색상 관련 변경 없음 - LocalContentColor 상속)
            if (isPlaying) {
                IconButton(onClick = onPause) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = stringResource(ResourceR.string.pauseDescription)
                    )
                }
                IconButton(onClick = onStop) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = stringResource(ResourceR.string.stopDescription)
                    )
                }
            } else if (isPaused) {
                IconButton(onClick = onResume) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(ResourceR.string.playDescription)
                    )
                }
            } else {
                IconButton(onClick = onPlay) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(ResourceR.string.playDescription)
                    )
                }
            }

            // 업로드 버튼/인디케이터 (색상 관련 변경 없음)
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onUpload) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = stringResource(ResourceR.string.uploadDescription)
                    )
                }
            }

            // 삭제 버튼
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(ResourceR.string.deleteDescription),
                    // MaterialTheme의 표준 error 색상을 사용하는 것은 매우 좋은 방법입니다.
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


//파일 타임스탬프를 읽기 좋은 형식으로 변환하는 함수
fun formatDate(millis: Long): String {
    val date = Date(millis)
    val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}