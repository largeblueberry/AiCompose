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
import com.largeblueberry.aicompose.R

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
                Text(text = stringResource(id = R.string.app_name),
                    fontSize = 24.sp, fontWeight = FontWeight.Bold)
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        CardViewScreen(
            iconResId = R.drawable.ic_mic,
            mainText = stringResource(id = R.string.recordMainText),
            subText = stringResource(id = R.string.recordSubText),
            applyTint = true,
            onClick = { navController.navigate("record_route") }
        )

        CardViewScreen(
            iconResId = R.drawable.ic_music_note,
            mainText = stringResource(id = R.string.libraryMainText),
            subText = stringResource(id = R.string.librarySubText),
            applyTint = false,
            onClick = { navController.navigate("library_route") }
        )

        CardViewScreen(
            iconResId = R.drawable.ic_settings,
            mainText = stringResource(id = R.string.settingsMainText),
            subText = stringResource(id = R.string.settingsSubText),
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