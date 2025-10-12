package com.largeblueberry.aicompose.feature_auth.ui.util

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.core_ui.customColors
import com.largeblueberry.resources.R as ResourcesR

@Composable
fun LoginCard(
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            // 1. 카드 배경색: MaterialTheme.customColors를 통해 접근
            containerColor = MaterialTheme.customColors.cardViewBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GoogleSignInButton(
                onClick = onGoogleSignIn,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    // 2. 구분선 색상: MaterialTheme.customColors를 통해 접근
                    color = MaterialTheme.customColors.utilHorizontalDivider
                )
                Text(
                    text = stringResource(id = ResourcesR.string.utilOr),
                    // 3. "또는" 텍스트 색상: MaterialTheme.customColors를 통해 접근
                    color = MaterialTheme.customColors.settingUtilColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    // 2. 구분선 색상: MaterialTheme.customColors를 통해 접근
                    color = MaterialTheme.customColors.utilHorizontalDivider
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(
                    text = stringResource(id = ResourcesR.string.userSkipButton),
                    // 4. 색상 지정 없음 -> MaterialTheme.colorScheme.primary가 자동으로 적용됨
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
