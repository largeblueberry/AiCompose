package com.largeblueberry.aicompose.feature_auth.ui.util

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.core_ui.AppBlack
import com.largeblueberry.core_ui.AppWhite
import com.largeblueberry.core_ui.googleButtonBorderColor
import com.largeblueberry.core_ui.googleDisabledContainerColor
import com.largeblueberry.core_ui.googleDisabledContentColor
import com.largeblueberry.ui.R
import com.largeblueberry.resources.R as ResourceR


@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean = true
){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppWhite, // 흰색 배경
            contentColor = AppBlack, // 검은색 글자
            disabledContainerColor = googleDisabledContainerColor,
            disabledContentColor = googleDisabledContentColor
        ),
        border = BorderStroke(1.dp, googleButtonBorderColor),
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = stringResource(id = ResourceR.string.googleLogo),
                modifier = Modifier.size(24.dp) // 로고 크기 조정 (구글 가이드라인 18-24dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(id = ResourceR.string.googleLogin),
                fontSize = 15.sp, // 구글 가이드라인에 맞춰 폰트 크기 조정 (14-16sp)
                fontWeight = FontWeight.Medium,
                color = AppBlack
            )
        }
    }
}