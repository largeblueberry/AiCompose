package com.largeblueberry.aicompose.feature_auth.ui.util

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.largeblueberry.core_ui.stringResource
import com.largeblueberry.resources.R

// 재인증 필요 다이얼로그
@Composable
fun ReauthenticationRequiredDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(stringResource(R.string.reauth_required_title)) },
        text = {
            Text(
                stringResource(R.string.reauth_required_message),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.reauth_required_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.reauth_cancel))
            }
        }
    )
}

// 재인증 진행 중 다이얼로그
@Composable
fun ReauthenticationLoadingDialog() {
    AlertDialog(
        onDismissRequest = { /* 진행 중에는 닫을 수 없음 */ },
        title = { Text(stringResource(R.string.reauth_loading_title)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.reauth_loading_message),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = { /* 버튼 없음 */ }
    )
}


// 재인증 에러 다이얼로그
@Composable
fun ReauthenticationErrorDialog(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(stringResource(R.string.reauth_error_title)) },
        text = {
            Text(
                stringResource(R.string.reauth_error_message_prefix) + errorMessage,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text(stringResource(R.string.reauth_error_retry))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.reauth_cancel))
            }
        }
    )
}