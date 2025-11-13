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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.largeblueberry.record.ui.RecordingState
import com.largeblueberry.resources.R as ResourcesR

object Theme {
    val customColors: CustomColors
        @Composable
        get() = LocalCustomColors.current
}

@Composable
fun RecordScreenUi(
    isRecording: Boolean,
    recordingStateText: RecordingState,
    onRecordToggleClick: () -> Unit,
    onViewRecordingsClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isSmallScreen = screenHeight < 600.dp

    // 화면 크기에 따른 동적 크기 조정
    val micButtonSize = if (isSmallScreen) 120.dp else 160.dp
    val micIconSize = if (isSmallScreen) 48.dp else 64.dp
    val titleFontSize = if (isSmallScreen) 24.sp else 28.sp
    val topPadding = if (isSmallScreen) 24.dp else 48.dp
    val buttonHeight = if (isSmallScreen) 48.dp else 56.dp

    val stateText = when (recordingStateText) {
        RecordingState.WAITING -> stringResource(ResourcesR.string.recordWaiting)
        RecordingState.RECORDING -> stringResource(ResourcesR.string.recording)
        RecordingState.COMPLETED -> stringResource(ResourcesR.string.recordComplete)
        RecordingState.FAILED_FILE_PATH_NOT_FOUND -> stringResource(ResourcesR.string.recordFailedFilePath)
        RecordingState.FAILED_START -> stringResource(ResourcesR.string.recordFailedStart)
        RecordingState.FAILED_STOP -> stringResource(ResourcesR.string.recordFailedStop)
    }

    // 녹음 중일 때의 애니메이션
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

    // 대기 중일 때의 애니메이션 (메인 버튼 + 배경 리플)
    val waitingTransition = rememberInfiniteTransition(label = "waiting_animation")
    val waitingPulseScale by waitingTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f, // 더 눈에 띄게 변경
        animationSpec = infiniteRepeatable(
            animation = tween(1200), // 약간 더 빠르게
            repeatMode = RepeatMode.Reverse
        ),
        label = "waiting_pulse_scale"
    )
    val waitingRippleScale by waitingTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f, // 배경원은 더 크게 퍼짐
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "waiting_ripple_scale"
    )
    val waitingRippleAlpha by waitingTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f, // 배경원은 사라짐
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "waiting_ripple_alpha"
    )

    // 상태에 따라 적용할 스케일과 알파 값 결정
    val buttonScale = if (isRecording) {
        recordingPulseScale
    } else if (recordingStateText == RecordingState.WAITING) {
        waitingPulseScale
    } else {
        1f
    }
    val buttonAlpha = if (isRecording) recordingPulseAlpha else 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 제목 영역
        Spacer(modifier = Modifier.height(topPadding))
        Text(
            text = stateText,
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // 중앙 마이크 버튼을 위한 Spacer (가변적)
        Spacer(modifier = Modifier.weight(1f))

        // 마이크 버튼 영역
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                // 배경 리플 효과 (대기 상태에서만 보임)
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

                // 메인 마이크 버튼
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
                        imageVector = Icons.Default.Mic,
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

            // 프로그레스 바 또는 공간
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

        // 하단 버튼을 위한 Spacer (가변적)
        Spacer(modifier = Modifier.weight(1f))

        // 하단 버튼 영역
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

        // 하단 버튼이 너무 아래에 붙지 않도록 가중치를 주어 공간을 확보합니다.
        Spacer(modifier = Modifier.weight(0.3f))
    }
}

@Preview
@Composable
fun RecordScreenPreview() {
    RecordScreenUi(
        isRecording = false,
        recordingStateText = RecordingState.WAITING,
        onRecordToggleClick = {},
        onViewRecordingsClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun RecordScreenUiRecordingPreview() {
    MaterialTheme {
        RecordScreenUi(
            isRecording = true,
            recordingStateText = RecordingState.RECORDING,
            onRecordToggleClick = {},
            onViewRecordingsClick = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
fun RecordScreenUiSmallScreenPreview() {
    MaterialTheme {
        RecordScreenUi(
            isRecording = false,
            recordingStateText = RecordingState.WAITING,
            onRecordToggleClick = {},
            onViewRecordingsClick = {}
        )
    }
}