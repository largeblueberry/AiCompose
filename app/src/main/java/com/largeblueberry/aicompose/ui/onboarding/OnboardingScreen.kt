package com.largeblueberry.aicompose.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.largeblueberry.aicompose.ui.splash.SplashScreen
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    onPermissionRequest: () -> Unit,
    onComplete: () -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }
    var showPermissionDialog by remember { mutableStateOf(false) }

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
                onPermissionRequest = {
                    showPermissionDialog = true
                },
                onComplete = onComplete
            )
        }
    }

    // Permission Dialog
    if (showPermissionDialog) {
        PermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onConfirm = {
                showPermissionDialog = false
                onPermissionRequest()
            }
        )
    }
}