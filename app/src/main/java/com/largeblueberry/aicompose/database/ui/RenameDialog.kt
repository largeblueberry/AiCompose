package com.largeblueberry.aicompose.database.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("파일 이름 변경") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("새 이름") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newName.isNotBlank() && newName != currentName) {
                        onConfirm(newName.trim())
                    }
                }
            ) { Text("확인") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}