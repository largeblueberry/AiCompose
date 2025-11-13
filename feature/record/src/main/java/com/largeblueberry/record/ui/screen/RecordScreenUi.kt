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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.core_ui.CustomColors
import com.largeblueberry.core_ui.LocalCustomColors
import com.largeblueberry.record.ui.RecordingState
import com.largeblueberry.resources.R as ResourcesR

/**
 * 녹음 화면의 UI 레이아웃을 정의하는 Composable 함수.
 * 상태와 이벤트 콜백을 파라미터로 받습니다.
 */
// 이전 답변과 마찬가지로 Theme 객체를 사용하면 편리합니다.
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
    // 녹음 상태에 따른 텍스트 설정 (기존 코드와 동일)
    val stateText = when (recordingStateText) {
        RecordingState.WAITING -> stringResource(ResourcesR.string.recordWaiting)
        RecordingState.RECORDING -> stringResource(ResourcesR.string.recording)
        RecordingState.COMPLETED -> stringResource(ResourcesR.string.recordComplete)
        RecordingState.FAILED_FILE_PATH_NOT_FOUND -> stringResource(ResourcesR.string.recordFailedFilePath)
        RecordingState.FAILED_START -> stringResource(ResourcesR.string.recordFailedStart)
        RecordingState.FAILED_STOP -> stringResource(ResourcesR.string.recordFailedStop)
    }

    // 녹음 중 애니메이션을 위한 무한 전환
    val infiniteTransition = rememberInfiniteTransition(label = "recording_animation")

    // 펄스 애니메이션 (크기 변화)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // 투명도 애니메이션
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 1. 화면 배경색을 테마의 background 색상으로 변경
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stateText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 중앙 영역 : 마이크 버튼 (클릭 가능)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clickable { onRecordToggleClick() }
                    .background(
                        color = if (isRecording) Theme.customColors.recordingRed else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .scale(if (isRecording) pulseScale else 1f)
                    .graphicsLayer(alpha = if (isRecording) pulseAlpha else 1f),
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
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                onClick = onViewRecordingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(ResourcesR.string.gotoLibrary),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

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

