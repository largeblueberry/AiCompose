package com.largeblueberry.aicompose.feature_auth.ui.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.largeblueberry.ui.R


@Composable
fun AppLogo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(
            painter = painterResource(id = R.drawable.eareamsplash),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            contentScale = ContentScale.Fit
        )
    }
}