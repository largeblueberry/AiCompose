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
                    text = record.filename,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.clickable { onRename() },
                    color = Theme.customColors.cardViewMainText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.duration,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Theme.customColors.cardViewSubText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(record.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Theme.customColors.cardViewSubText
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

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

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(ResourceR.string.deleteDescription),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun formatDate(millis: Long): String {
    val date = Date(millis)
    val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}