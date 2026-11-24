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
import com.largeblueberry.domain.model.LibraryModel
import com.largeblueberry.domain.model.TranscriptionFailure
import com.largeblueberry.library.ui.model.RecordTranscription
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
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
    transcription: RecordTranscription,
    isPaused: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit,
    onTranscribe: () -> Unit,
    onOpenScore: (scoreId: String) -> Unit,
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
                    text = stringResource(ResourceR.string.libraryItemMeta, record.duration, record.bpm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Theme.customColors.cardViewSubText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(record.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Theme.customColors.cardViewSubText
                )
                transcriptionStatusText(transcription)?.let { status ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (transcription is RecordTranscription.Failed) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                }
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

            TranscriptionAction(transcription, onTranscribe, onOpenScore)

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

/** 채보 버튼 자리: 상태에 따라 만들기·진행 표시·악보 보기 */
@Composable
private fun TranscriptionAction(
    transcription: RecordTranscription,
    onTranscribe: () -> Unit,
    onOpenScore: (scoreId: String) -> Unit,
) {
    when (transcription) {
        RecordTranscription.Idle, is RecordTranscription.Failed -> IconButton(onClick = onTranscribe) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = stringResource(ResourceR.string.transcribeDescription)
            )
        }
        is RecordTranscription.Done -> IconButton(onClick = { onOpenScore(transcription.scoreId) }) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = stringResource(ResourceR.string.viewScoreDescription),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        is RecordTranscription.Running -> Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { transcription.progress },
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
        RecordTranscription.Queued, RecordTranscription.Saving ->
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
    }
}

@Composable
private fun transcriptionStatusText(transcription: RecordTranscription): String? = when (transcription) {
    RecordTranscription.Idle, is RecordTranscription.Done -> null
    RecordTranscription.Queued -> stringResource(ResourceR.string.transcribeQueued)
    is RecordTranscription.Running ->
        stringResource(ResourceR.string.transcribeRunning, (transcription.progress * 100).roundToInt())
    RecordTranscription.Saving -> stringResource(ResourceR.string.transcribeSaving)
    is RecordTranscription.Failed -> when (transcription.reason) {
        TranscriptionFailure.NO_NOTES -> stringResource(ResourceR.string.transcribeNoNotes)
        TranscriptionFailure.RECORD_GONE, TranscriptionFailure.OTHER -> stringResource(ResourceR.string.transcribeError)
    }
}

private val dateFormatThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat {
        return SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    }
}

fun formatDate(millis: Long): String {
    val formatter = dateFormatThreadLocal.get() ?: SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(millis))
}