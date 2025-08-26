package com.largeblueberry.aicompose.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.largeblueberry.ui.R
import com.largeblueberry.core_ui.AppWhite


@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppWhite,
                        AppWhite
                    )// 이미지 기준 위아래 색 통일
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 로고 이미지
        Image(
            painter = painterResource(id = R.drawable.eareamsplash),
            contentDescription = "앱 로고",
            modifier = Modifier.size(320.dp), // 로고 크기 조절
            contentScale = ContentScale.Fit
        )
    }
}