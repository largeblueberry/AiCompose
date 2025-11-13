package com.largeblueberry.aicompose.feature_auth.ui.util

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme // 👈 1. import 추가
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
    // 👈 2. 현재 테마가 다크 모드인지 확인
    val isDarkTheme = isSystemInDarkTheme()

    // 👈 3. 테마에 따라 버튼 색상과 테두리 색상을 결정
    val containerColor = if (isDarkTheme) AppBlack else AppWhite
    val contentColor = if (isDarkTheme) AppWhite else AppBlack
    val borderColor = if (isDarkTheme) googleButtonBorderColor.copy(alpha = 0.8f) else googleButtonBorderColor

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor, // 테마에 맞는 배경색 적용
            contentColor = contentColor,     // 테마에 맞는 콘텐츠색 적용
            disabledContainerColor = googleDisabledContainerColor,
            disabledContentColor = googleDisabledContentColor
        ),
        border = BorderStroke(1.dp, borderColor), // 테마에 맞는 테두리색 적용
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
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(id = ResourceR.string.googleLogin),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
                // 👈 4. color 속성 제거 (Button의 contentColor가 자동으로 적용됨)
            )
        }
    }
}
