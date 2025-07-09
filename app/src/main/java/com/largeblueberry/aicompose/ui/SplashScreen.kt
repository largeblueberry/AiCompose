package com.largeblueberry.aicompose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.largeblueberry.aicompose.R

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6C6CFF), // 위쪽 색
                        Color(0xFF5B6EE1)  // 아래쪽 색
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 로고 이미지
        Image(
            painter = painterResource(id = R.drawable.eareamsplash), // 파일명에 맞게 변경
            contentDescription = "앱 로고",
            modifier = Modifier.size(320.dp), // 로고 크기 조절
            contentScale = ContentScale.Fit
        )
    }
}