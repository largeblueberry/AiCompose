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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.core_ui.AppBackground
import com.largeblueberry.core_ui.AppTextDark
import com.largeblueberry.core_ui.ProgressBarBackgroundTint
import com.largeblueberry.core_ui.RecordingRed
import com.largeblueberry.core_ui.AppPrimaryBlue
import com.largeblueberry.record.ui.RecordingState
import com.largeblueberry.resources.R as ResourcesR

/**
 * 녹음 화면의 UI 레이아웃을 정의하는 Composable 함수.
 * 상태와 이벤트 콜백을 파라미터로 받습니다.
 */

@Composable
fun RecordScreenUi(
    isRecording: Boolean,
    recordingStateText: RecordingState,
    onRecordToggleClick: () -> Unit,
    onViewRecordingsClick: () -> Unit
){
    // 녹음 상태에 따른 텍스트 설정
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
            .background(AppBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally // 자식 컴포넌트들을 가운데 정렬
    ){
// 상단 상태 텍스트
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stateText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AppTextDark
        )
        // 마이크 아이콘 (원 배경 포함)
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(112.dp) // width, height "112dp"
                .background(AppPrimaryBlue, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(48.dp)
            )
        }

        // 진행 바
        Spacer(modifier = Modifier.height(32.dp))
        if (isRecording) { // 녹음 중일 때만 진행 바 표시
            LinearProgressIndicator(
                progress = {0.5f}, // 그냥 항상 고정
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = AppPrimaryBlue,
                trackColor = ProgressBarBackgroundTint
            )
        } else {
            // 녹음 중이 아닐 때는 진행 바를 숨기거나 다른 UI를 표시할 수 있습니다.
            // 여기서는 단순히 공간을 차지하지 않도록 합니다.
            Spacer(modifier = Modifier.height(8.dp)) // 진행 바가 있을 때와 동일한 높이 유지
        }
        // 녹음 시작/중지 토글 버튼
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onRecordToggleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) RecordingRed else AppPrimaryBlue // 배경색 변경
            ),
            shape = MaterialTheme.shapes.small // 기본 버튼 모양 사용
        ) {
            Text(
                text = if (isRecording){
                    stringResource(ResourcesR.string.recordStop)
                } else{
                    stringResource(ResourcesR.string.recordStart)
                },
                color = White, // 텍스트 색상
                fontWeight = FontWeight.Bold // 텍스트 스타일
            )
        }

        // 녹음 파일 보기 버튼
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onViewRecordingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = AppPrimaryBlue
            ),
            border = BorderStroke(1.dp, AppPrimaryBlue),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = stringResource(ResourcesR.string.gotoLibrary),
                fontWeight = FontWeight.Bold
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

