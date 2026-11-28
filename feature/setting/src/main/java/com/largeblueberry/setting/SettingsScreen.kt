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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * 앱 정보 헤더. 로고는 투명 배경 선화(eareamlogo_line)를 테마 글자색으로 칠해 라이트·다크 모두에 맞춘다.
 * 설정 항목이 첫 화면에 보이도록 한 줄로 둔다.
 */
@Composable
private fun AppInfoCard() {
    val colors = MaterialTheme.customColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.appWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.eareamlogo_line),
                contentDescription = stringResource(ResourceR.string.eareamLogoDescription),
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(colors.appBlack)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = ResourceR.string.app_name),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.titleBlueDark
                )
                Text(
                    text = stringResource(id = ResourceR.string.app_subtitle),
                    fontSize = 13.sp,
                    color = colors.subtitleBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(ResourceR.string.version_info, BuildConfig.VERSION_NAME),
                    fontSize = 12.sp,
                    color = colors.settingUtilColor
                )
            }
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
    SettingSection(title = stringResource(ResourceR.string.settingAboutSection)) {
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
