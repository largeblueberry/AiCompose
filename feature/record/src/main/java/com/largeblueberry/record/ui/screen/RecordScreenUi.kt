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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.core_ui.AppBackground
import com.largeblueberry.core_ui.AppTextDark
import com.largeblueberry.core_ui.ProgressBarBackgroundTint
import com.largeblueberry.core_ui.RecordingRed
import com.largeblueberry.core_ui.AppPrimaryBlue

/**
 * 녹음 화면의 UI 레이아웃을 정의하는 Composable 함수.
 * 상태와 이벤트 콜백을 파라미터로 받습니다.
 */

@Composable
fun RecordScreenUi(
    isRecording: Boolean,
    recordingStateText: String,
    onRecordToggleClick: () -> Unit,
    onViewRecordingsClick: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally // 자식 컴포넌트들을 가운데 정렬
    ){
// 상단 상태 텍스트
        Spacer(modifier = Modifier.height(48.dp)) // XML의 layout_marginTop="48dp"
        Text(
            text = recordingStateText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AppTextDark
        )
        // 마이크 아이콘 (원 배경 포함)
        Spacer(modifier = Modifier.height(32.dp)) // XML의 layout_marginTop="32dp"
        Box(
            modifier = Modifier
                .size(112.dp) // width, height "112dp"
                .background(AppPrimaryBlue, shape = CircleShape), // round_blue_bg 대체
            contentAlignment = Alignment.Center // ImageView의 layout_gravity="center"
        ) {
            Icon(
                imageVector = Icons.Default.Mic, // ic_mic 대체. Material Icons 사용
                contentDescription = "Microphone Icon",
                tint = White, // app:tint="@android:color/white" 대체
                modifier = Modifier.size(48.dp) // width, height "48dp"
            )
        }

        // 진행 바
        Spacer(modifier = Modifier.height(32.dp)) // XML의 layout_marginTop="32dp"
        if (isRecording) { // 녹음 중일 때만 진행 바 표시
            LinearProgressIndicator(
                progress = {0.5f}, // 그냥 항상 고정
                modifier = Modifier
                    .fillMaxWidth() // layout_width="0dp" (match_parent)
                    .height(8.dp), // layout_height="8dp"
                color = AppPrimaryBlue, // progressTint="#4F8CFF"
                trackColor = ProgressBarBackgroundTint
            )
        } else {
            // 녹음 중이 아닐 때는 진행 바를 숨기거나 다른 UI를 표시할 수 있습니다.
            // 여기서는 단순히 공간을 차지하지 않도록 합니다.
            Spacer(modifier = Modifier.height(8.dp)) // 진행 바가 있을 때와 동일한 높이 유지 (선택 사항)
        }
        // 녹음 시작/중지 토글 버튼
        Spacer(modifier = Modifier.height(40.dp)) // XML의 layout_marginTop="40dp"
        Button(
            onClick = onRecordToggleClick,
            modifier = Modifier
                .fillMaxWidth() // layout_width="0dp" (match_parent)
                .height(48.dp), // layout_height="48dp"
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) RecordingRed else AppPrimaryBlue // 배경색 변경
            ),
            shape = MaterialTheme.shapes.small // 기본 버튼 모양 사용 (cornerRadius가 없으므로)
        ) {
            Text(
                text = if (isRecording) "녹음 중지" else "녹음 시작",
                color = White, // 텍스트 색상
                fontWeight = FontWeight.Bold // 텍스트 스타일
            )
        }

        // 녹음 파일 보기 버튼
        Spacer(modifier = Modifier.height(12.dp)) // XML의 layout_marginBottom="12dp" (이전 버튼과의 간격)
        Button(
            onClick = onViewRecordingsClick,
            modifier = Modifier
                .fillMaxWidth() // layout_width="0dp" (match_parent)
                .height(48.dp), // layout_height="48dp"
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, // 배경 투명
                contentColor = AppPrimaryBlue // 텍스트 색상
            ),
            border = BorderStroke(1.dp, AppPrimaryBlue), // 테두리 추가 (bg_outline 대체)
            shape = MaterialTheme.shapes.small // 기본 버튼 모양 사용
        ) {
            Text(
                text = "녹음 파일 보기",
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
        recordingStateText = "대기 중...",
        onRecordToggleClick = {},
        onViewRecordingsClick = {}
    )
}

