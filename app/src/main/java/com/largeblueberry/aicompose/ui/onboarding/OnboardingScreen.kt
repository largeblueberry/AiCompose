package com.largeblueberry.aicompose.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OnboardingScreen(
    showSettingsDialog: Boolean,
    onDismissSettingsDialog: () -> Unit,
    onGoToSettingsClick: () -> Unit,
    onPermissionRequest: () -> Unit,
    onComplete: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        OnboardingPagerScreen(
            onPermissionRequest = onPermissionRequest,
            onComplete = onComplete
        )
    }

    if (showSettingsDialog) {
        PermissionDialog(
            onDismiss = onDismissSettingsDialog,
            onConfirm = onGoToSettingsClick
        )
    }
}