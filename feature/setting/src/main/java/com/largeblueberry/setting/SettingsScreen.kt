package com.largeblueberry.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.core_ui.LightCustomColors
import com.largeblueberry.core_ui.component.EareamTopAppBar
import com.largeblueberry.core_ui.customColors
import com.largeblueberry.ui.R
import com.largeblueberry.setting.util.SettingItem
import com.largeblueberry.setting.util.SettingSection
import com.largeblueberry.resources.R as ResourceR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToTheme: () -> Unit = {},
    onNavigateToBugReport: () -> Unit = {},
    onNavigateToServiceTerm: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.customColors.settingBackground)
    ) {
        EareamTopAppBar(
            title = stringResource(ResourceR.string.settingTitle),
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 앱 정보 카드
            item {
                AppInfoCard()
            }

            // 앱 설정 섹션
            item {
                AppSettingsSection(
                    onNavigateToLanguage = onNavigateToLanguage,
                    onNavigateToTheme = onNavigateToTheme
                )
            }

            // 앱 정보 섹션
            item {
                AboutSection(
                    onNavigateToServiceTerm = onNavigateToServiceTerm,
                    onNavigateToAbout = onNavigateToAbout
                )
            }
        }
    }
}

@Composable
private fun AppInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.customColors.appWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 앱 로고
            Image(
                painter = painterResource(id = R.drawable.eareamlogo),
                contentDescription = stringResource(ResourceR.string.eareamLogoDescription),
                modifier = Modifier
                    .size(140.dp)
                    .padding(8.dp)
                    .offset(x = (30).dp, y = 0.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 앱 제목
            Text(
                text = stringResource(id = ResourceR.string.app_name),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = LightCustomColors.titleBlueDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 부제목
            Text(
                text = stringResource(id = ResourceR.string.app_subtitle),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = LightCustomColors.subtitleBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 버전 텍스트
            Text(
                text = "버전 ${BuildConfig.VERSION_NAME}",
                fontSize = 12.sp,
                color = MaterialTheme.customColors.settingUtilColor
            )
        }
    }
}

@Composable
fun AppSettingsSection(
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToTheme: () -> Unit = {}
) {
    SettingSection(title = stringResource(ResourceR.string.appSetting)) {
        SettingItem(
            icon = Icons.Default.Language,
            title = stringResource(ResourceR.string.languageMainText),
            subtitle = stringResource(ResourceR.string.languageSubText),
            onClick = onNavigateToLanguage
        )

        SettingItem(
            icon = Icons.Default.Palette,
            title = stringResource(ResourceR.string.themeMainText),
            subtitle = stringResource(ResourceR.string.themeSubText),
            onClick = onNavigateToTheme
        )
    }
}

@Composable
private fun AboutSection(
    onNavigateToServiceTerm: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    SettingSection(title = "정보") {
        SettingItem(
            icon = Icons.Default.Description,
            title = stringResource(ResourceR.string.serviceTermMainText),
            subtitle = stringResource(ResourceR.string.serviceTermSubText),
            onClick = onNavigateToServiceTerm
        )

        SettingItem(
            icon = Icons.AutoMirrored.Filled.ContactSupport,
            title = stringResource(ResourceR.string.aboutEareamMainText),
            subtitle = stringResource(ResourceR.string.aboutEareamSubText),
            onClick = onNavigateToAbout
        )
    }
}
