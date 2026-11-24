package com.largeblueberry.record.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.core_ui.CustomColors
import com.largeblueberry.core_ui.LocalCustomColors
import com.largeblueberry.domain.model.RecorderState
import com.largeblueberry.record.ui.RecordUiState
import com.largeblueberry.record.ui.RecordingState
import com.largeblueberry.resources.R as ResourcesR

object Theme {
    val customColors: CustomColors
        @Composable
        get() = LocalCustomColors.current
}

@Composable
fun RecordScreenUi(
    uiState: RecordUiState,
    onRecordToggleClick: () -> Unit,
    onBpmChange: (delta: Int) -> Unit,
    onViewRecordingsClick: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    // 카운트인 중에도 버튼은 "멈춤"이다(녹음 세션이 열려 있다)
    val isRecording = uiState.isActive
    val recordingStateText = uiState.status
    val recorder = uiState.recorder
    val currentBeat = when (recorder) {
        is RecorderState.CountIn -> recorder.beat
        is RecorderState.Recording -> recorder.beat
        RecorderState.Idle, RecorderState.Failed -> null
    }
    val clickAudible = when (recorder) {
        is RecorderState.CountIn -> recorder.clickAudible
        is RecorderState.Recording -> recorder.clickAudible
        RecorderState.Idle, RecorderState.Failed -> null
    }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isSmallScreen = screenHeight < 600.dp

    val micButtonSize = if (isSmallScreen) 120.dp else 160.dp
    val micIconSize = if (isSmallScreen) 48.dp else 64.dp
    val titleFontSize = if (isSmallScreen) 24.sp else 28.sp
    val topPadding = if (isSmallScreen) 24.dp else 48.dp
    val buttonHeight = if (isSmallScreen) 48.dp else 56.dp

    val stateText = when (recordingStateText) {
        RecordingState.WAITING -> stringResource(ResourcesR.string.recordWaiting)
        RecordingState.COUNT_IN -> stringResource(
            ResourcesR.string.recordCountIn,
            (recorder as? RecorderState.CountIn)?.beatsLeft ?: 0
        )
        RecordingState.RECORDING -> stringResource(ResourcesR.string.recording)
        RecordingState.COMPLETED -> stringResource(ResourcesR.string.recordComplete)
        RecordingState.FAILED_START -> stringResource(ResourcesR.string.recordFailedStart)
        RecordingState.FAILED_STOP -> stringResource(ResourcesR.string.recordFailedStop)
        RecordingState.TOO_SHORT -> stringResource(ResourcesR.string.recordTooShort)
        RecordingState.CANCELLED -> stringResource(ResourcesR.string.recordCancelled)
        RecordingState.INTERRUPTED -> stringResource(ResourcesR.string.recordInterrupted)
    }

    val recordingTransition = rememberInfiniteTransition(label = "recording_animation")
    val recordingPulseScale by recordingTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val recordingPulseAlpha by recordingTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val waitingTransition = rememberInfiniteTransition(label = "waiting_animation")
    val waitingPulseScale by waitingTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waiting_pulse_scale"
    )
    val waitingRippleScale by waitingTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "waiting_ripple_scale"
    )
    val waitingRippleAlpha by waitingTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "waiting_ripple_alpha"
    )

    val buttonScale = if (isRecording) {
        recordingPulseScale
    } else if (recordingStateText == RecordingState.WAITING) {
        waitingPulseScale
    } else {
        1f
    }
    val buttonAlpha = if (isRecording) recordingPulseAlpha else 1f

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(topPadding))
            Text(
                text = stateText,
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            BpmSelector(bpm = uiState.bpm, enabled = !isRecording, onBpmChange = onBpmChange)
            Spacer(modifier = Modifier.height(16.dp))
            BeatIndicator(currentBeat = currentBeat, beatsPerBar = 4)
            if (clickAudible != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        if (clickAudible) ResourcesR.string.recordClickHintHeadphones
                        else ResourcesR.string.recordClickHintSpeaker
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    if (recordingStateText == RecordingState.WAITING) {
                        Box(
                            modifier = Modifier
                                .size(micButtonSize)
                                .scale(waitingRippleScale)
                                .graphicsLayer(alpha = waitingRippleAlpha)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(micButtonSize)
                            .clickable { onRecordToggleClick() }
                            .background(
                                color = if (isRecording) Theme.customColors.recordingRed else MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .scale(buttonScale)
                            .graphicsLayer(alpha = buttonAlpha),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecording) {
                                stringResource(ResourcesR.string.recordStop)
                            } else {
                                stringResource(ResourcesR.string.recordStart)
                            },
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(micIconSize)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isSmallScreen) 24.dp else 32.dp))

                if (isRecording) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Theme.customColors.progressBarBackground
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onViewRecordingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .height(buttonHeight),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(ResourcesR.string.gotoLibrary),
                    fontSize = if (isSmallScreen) 14.sp else 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}

@Composable
private fun BpmSelector(bpm: Int, enabled: Boolean, onBpmChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(ResourcesR.string.recordBpmLabel),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = { onBpmChange(-5) }, enabled = enabled) {
                Icon(Icons.Default.Remove, contentDescription = stringResource(ResourcesR.string.recordBpmSlower))
            }
            Text(
                text = stringResource(ResourcesR.string.recordBpmValue, bpm),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(min = 120.dp)
                    .padding(horizontal = 8.dp)
            )
            FilledTonalIconButton(onClick = { onBpmChange(5) }, enabled = enabled) {
                Icon(Icons.Default.Add, contentDescription = stringResource(ResourcesR.string.recordBpmFaster))
            }
        }
    }
}

/** 마디 안 박 위치. 녹음 중 클릭 소리가 없을 때 박자를 따라가는 기준이다. 첫 박은 크게. */
@Composable
private fun BeatIndicator(currentBeat: Int?, beatsPerBar: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until beatsPerBar) {
            val active = currentBeat == i
            Box(
                modifier = Modifier
                    .size(if (i == 0) 22.dp else 16.dp)
                    .background(
                        color = if (active) Theme.customColors.recordingRed else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Preview
@Composable
fun RecordScreenPreview() {
    RecordScreenUi(
        uiState = RecordUiState(),
        onRecordToggleClick = {},
        onBpmChange = {},
        onViewRecordingsClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun RecordScreenUiRecordingPreview() {
    MaterialTheme {
        RecordScreenUi(
            uiState = RecordUiState(
                recorder = RecorderState.Recording(beat = 2, elapsedMs = 3_000, clickAudible = false),
                status = RecordingState.RECORDING
            ),
            onRecordToggleClick = {},
            onBpmChange = {},
            onViewRecordingsClick = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
fun RecordScreenUiSmallScreenPreview() {
    MaterialTheme {
        RecordScreenUi(
            uiState = RecordUiState(
                recorder = RecorderState.CountIn(beatsLeft = 3, beat = 1, clickAudible = true),
                status = RecordingState.COUNT_IN
            ),
            onRecordToggleClick = {},
            onBpmChange = {},
            onViewRecordingsClick = {}
        )
    }
}
