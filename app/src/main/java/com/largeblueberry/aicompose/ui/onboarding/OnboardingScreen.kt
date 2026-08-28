package com.largeblueberry.aicompose.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.largeblueberry.aicompose.ui.splash.SplashScreen
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    showSplash: Boolean,
    showSettingsDialog: Boolean,
    onDismissSettingsDialog: () -> Unit,
    onGoToSettingsClick: () -> Unit,
    onPermissionRequest: () -> Unit,
    onComplete: () -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000) // 2초 후 스플래시 화면 숨김
        showSplash = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Splash Screen
        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            SplashScreen()
        }

        // Onboarding Pager
        AnimatedVisibility(
            visible = !showSplash,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            OnboardingPagerScreen(
                onPermissionRequest = onPermissionRequest,
                onComplete = onComplete
            )
        }
    }

    if (showSettingsDialog) {
        PermissionDialog(
            onDismiss = onDismissSettingsDialog,
            onConfirm = onGoToSettingsClick
        )
    }
}