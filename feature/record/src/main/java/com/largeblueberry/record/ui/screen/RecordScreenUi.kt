package com.largeblueberry.record.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 1. 화면 배경색을 테마의 background 색상으로 변경
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 상태 텍스트
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stateText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            // 2. 텍스트 색상을 테마의 onBackground 색상으로 변경
            color = MaterialTheme.colorScheme.onBackground
        )
        // 마이크 아이콘 (원 배경 포함)
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(112.dp)
                // 3. 원 배경을 테마의 primary 색상으로 변경
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                // 4. 아이콘 색상을 테마의 onPrimary 색상으로 변경
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        // 진행 바
        Spacer(modifier = Modifier.height(32.dp))
        if (isRecording) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                // 5. 진행 바 색상을 테마의 primary 색상으로 변경
                color = MaterialTheme.colorScheme.primary,
                // 6. 진행 바 트랙 색상을 커스텀 테마의 색상으로 변경
                trackColor = Theme.customColors.progressBarBackground
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 녹음 시작/중지 토글 버튼
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onRecordToggleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                // 7. 녹음 상태에 따라 커스텀 색상과 표준 색상을 사용하도록 변경
                containerColor = if (isRecording) Theme.customColors.recordingRed else MaterialTheme.colorScheme.primary,
                // 8. 버튼 콘텐츠 색상을 onPrimary로 통일하여 일관성 확보
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = if (isRecording) {
                    stringResource(ResourcesR.string.recordStop)
                } else {
                    stringResource(ResourcesR.string.recordStart)
                },
                fontWeight = FontWeight.Bold
            )
        }

        // 녹음 파일 보기 버튼 (OutlinedButton으로 리팩토링하여 코드 간소화)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onViewRecordingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.small,
            // 9. OutlinedButton은 기본적으로 primary 색상을 사용하므로 색상 지정 코드가 불필요
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(ResourcesR.string.gotoLibrary),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary // 텍스트 색상 명시
            )
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

