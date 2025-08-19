package com.largeblueberry.aicompose.library.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.largeblueberry.aicompose.library.domainLayer.model.LibraryModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecordItem(
    record: LibraryModel,
    isPlaying: Boolean, // 현재 이 레코드가 재생 중인지 여부
    isUploading: Boolean,
    isPaused: Boolean,  // 추가: 현재 이 레코드가 일시정지 상태인지 여부
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit,
    onUpload: () -> Unit,
    onRename: () -> Unit,
    onResume: () -> Unit // 추가: 이어서 재생 콜백
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    modifier = Modifier.clickable { onRename() }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.duration,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(record.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // 재생/일시정지/정지 버튼 로직
            if (isPlaying) { // 현재 재생 중일 때
                IconButton(onClick = onPause) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "일시정지"
                    )
                }
                IconButton(onClick = onStop) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "정지"
                    )
                }
            } else if (isPaused) { // 재생 중이 아니지만 일시정지된 상태일 때 (같은 레코드)
                IconButton(onClick = onResume) { // 이어서 재생
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "재생"
                    )
                }
            } else { // 재생 중이 아니고 일시정지된 상태도 아닐 때 (새로운 재생 또는 다른 레코드)
                IconButton(onClick = onPlay) { // 처음부터 재생
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "재생"
                    )
                }
            }

            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onUpload) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "업로드"
                    )
                }
            }

            // 삭제 버튼
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
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