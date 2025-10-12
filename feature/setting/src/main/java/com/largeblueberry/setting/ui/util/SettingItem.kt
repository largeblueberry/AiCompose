package com.largeblueberry.setting.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.core_ui.SettingUtilColor
import com.largeblueberry.core_ui.SettingItemMainText
import com.largeblueberry.core_ui.UtilTextColor
import com.largeblueberry.core_ui.customColors

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    showArrow: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            // 1. 메인 아이콘 색상: customColors.utilTextColor로 변경
            tint = MaterialTheme.customColors.utilTextColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                // 2. 제목 텍스트 색상: customColors.settingItemMainText로 변경
                color = MaterialTheme.customColors.settingItemMainText
            )

            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    // 3. 부제 텍스트 색상: customColors.utilTextColor로 변경
                    color = MaterialTheme.customColors.utilTextColor
                )
            }
        }

        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                // 4. 화살표 아이콘 색상: customColors.settingUtilColor로 변경 (이전과 동일)
                tint = MaterialTheme.customColors.settingUtilColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
