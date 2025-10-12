package com.largeblueberry.aicompose.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.largeblueberry.core_ui.customColors

@Composable
fun CardViewScreen(
    iconResId : Int,
    mainText: String,
    subText: String,
    applyTint : Boolean = true,
    onClick : () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),

    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .background(
                        color = MaterialTheme.customColors.cardViewBackground,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ){
                Image(
                    painter = painterResource(iconResId), // 외부에서 받은 아이콘 사용
                    colorFilter = if (applyTint) ColorFilter.tint(MaterialTheme.colorScheme.onPrimary) else null,
                    contentDescription = null, // 접근성 고려하여 적절한 내용으로 변경 가능
                    modifier = Modifier.size(28.dp)
                )

            }

            Column(
                modifier = Modifier.padding(start = 16.dp)
            ){
                Text(
                    text = mainText,
                    color = MaterialTheme.customColors.cardViewMainText,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subText,
                    color = MaterialTheme.customColors.cardViewSubText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Light
                    )
                )
            }
        }

    }
}