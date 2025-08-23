package com.largeblueberry.aicompose.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.largeblueberry.aicompose.R


val BackgroundColor = Color(0xFFFFFFFF)

@Composable
fun MainScreen(navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        CardViewScreen(
            iconResId = R.drawable.ic_mic,
            mainText = "음성 녹음하기",
            subText = "순간의 영감을 빨리 기록해요.",
            applyTint = true,
            onClick = { navController.navigate("record_route") }
        )

        CardViewScreen(
            iconResId = R.drawable.ic_music_note,
            mainText = "내 작품",
            subText = "내가 만든 작품들을 확인해요.",
            applyTint = false,
            onClick = { navController.navigate("library_route") }
        )

        CardViewScreen(
            iconResId = R.drawable.ic_settings,
            mainText = "설정",
            subText = "앱 설정을 변경해요.",
            applyTint = true,
            onClick = { navController.navigate("settings_route") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(navController = rememberNavController())
}