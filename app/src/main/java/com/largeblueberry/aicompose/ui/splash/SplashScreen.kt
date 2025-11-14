package com.largeblueberry.aicompose.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.ui.R
import com.largeblueberry.core_ui.AppWhite
import com.largeblueberry.core_ui.LightCustomColors
import com.largeblueberry.resources.R as ResourceR

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
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 새로운 귀 + 음표 로고
            Image(
                painter = painterResource(id = R.drawable.eareamlogo), // 업로드한 로고 파일명
                contentDescription = null,
                modifier = Modifier
                    .size(210.dp)
                    .offset(x = (30).dp, y = 0.dp), // 원본 비율 유지하면서 적당한 크기
                contentScale = ContentScale.Crop, // Fit 대신 Crop 사용
                alignment = Alignment.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 앱 제목
            Text(
                text = stringResource(id = ResourceR.string.app_name),
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                color = LightCustomColors.titleBlueDark, // 파란색
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 부제목
            Text(
                text = stringResource(id = ResourceR.string.app_subtitle),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = LightCustomColors.subtitleBlue, // 연보라색
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp) // 긴 텍스트를 위한 패딩
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}
