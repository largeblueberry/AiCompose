package com.largeblueberry.aicompose.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.largeblueberry.resources.R as ResourceR
import com.largeblueberry.aicompose.R
import com.largeblueberry.navigation.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(text = stringResource(id = ResourceR.string.home_title),
                    fontSize = 20.sp, fontWeight = FontWeight.Bold)
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        CardViewScreen(
            iconResId = R.drawable.ic_mic,
            mainText = stringResource(id = ResourceR.string.recordMainText),
            subText = stringResource(id = ResourceR.string.recordSubText),
            applyTint = true,
            onClick = { navController.navigate("record_route") }
        )

        //라이브러리 화면은 녹음 화면임.
        CardViewScreen(
            iconResId = R.drawable.ic_waveform,
            mainText = stringResource(id = ResourceR.string.waveformMainText),
            subText = stringResource(id = ResourceR.string.waveformSubText),
            applyTint = false,
            onClick = { navController.navigate("library_route") }
        )

        // 이거는 악보 확인 화면으로 넘어감.
        CardViewScreen(
            iconResId = R.drawable.ic_music_note,
            mainText = stringResource(id = ResourceR.string.libraryMainText),
            subText = stringResource(id = ResourceR.string.librarySubText),
            applyTint = false,
            onClick = { navController.navigate(AppRoutes.SheetMusicScreen.route) }
        )

        CardViewScreen(
            iconResId = R.drawable.ic_settings,
            mainText = stringResource(id = ResourceR.string.settingsMainText),
            subText = stringResource(id = ResourceR.string.settingsSubText),
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