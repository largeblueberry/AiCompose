package com.largeblueberry.aicompose.ui.onboarding

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

// 권한 요청 다이얼로그
@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "권한 허용",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("앱이 정상적으로 작동하려면 다음 권한이 필요합니다:\n\n• 마이크 권한\n• 접근성 서비스")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("설정으로 이동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}