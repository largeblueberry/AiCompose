package com.largeblueberry.setting.bugreport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.largeblueberry.core_ui.customColors
import com.largeblueberry.navigation.SettingsNavigationActions
import com.largeblueberry.resources.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportScreen(
    modifier: Modifier = Modifier,
    navigationActions : SettingsNavigationActions = SettingsNavigationActions()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.customColors.settingBackground)
    ) {
        // 상단 앱바
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.bugreport),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = navigationActions.onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.backButtonContentDescription)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                // 2. 컨테이너 색상: 테마의 surface 색상을 사용
                containerColor = MaterialTheme.colorScheme.surface,
                // 제목 및 아이콘 색상도 자동으로 onSurface로 지정
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
