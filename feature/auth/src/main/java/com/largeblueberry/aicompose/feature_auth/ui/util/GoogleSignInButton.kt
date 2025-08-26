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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.ui.R


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
            containerColor = Color.White, // 흰색 배경
            contentColor = Color.Black, // 검은색 글자 (기본 텍스트 색상)
            disabledContainerColor = Color(0xFFE0E0E0), // 비활성화 시 연한 회색 배경
            disabledContentColor = Color(0xFF9E9E9E) // 비활성화 시 연한 회색 글자
        ),
        border = BorderStroke(1.dp, Color(0xFFDADCE0)), // 연한 회색 테두리
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp) // 로고 크기 조정 (구글 가이드라인 18-24dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Google 계정으로 로그인",
                fontSize = 15.sp, // 구글 가이드라인에 맞춰 폰트 크기 조정 (14-16sp)
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}