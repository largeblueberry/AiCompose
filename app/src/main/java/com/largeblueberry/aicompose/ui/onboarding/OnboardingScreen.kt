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
                // Pager에서 권한 요청이 필요할 때 Activity의 requestPermissions()를 직접 호출합니다.
                onPermissionRequest = onPermissionRequest,
                onComplete = onComplete
            )
        }
    }

    // Permission Dialog (상태를 MainActivity로부터 전달받아 표시)
    if (showSettingsDialog) {
        PermissionDialog(
            onDismiss = onDismissSettingsDialog,
            onConfirm = onGoToSettingsClick
        )
    }
}