package com.largeblueberry.setting.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.auth.model.AuthUiState
import com.largeblueberry.auth.model.UserCore
import com.largeblueberry.core_ui.AppBlack
import com.largeblueberry.core_ui.AppPrimaryBlue
import com.largeblueberry.core_ui.AppRed
import com.largeblueberry.core_ui.AppWhite
import com.largeblueberry.core_ui.SettingUtilColor
import com.largeblueberry.core_ui.SettingBackground
import com.largeblueberry.core_ui.SettingBasicUser
import com.largeblueberry.setting.BuildConfig
import com.largeblueberry.ui.R
import com.largeblueberry.setting.ui.util.SettingItem
import com.largeblueberry.setting.ui.util.SettingSection
import com.largeblueberry.setting.ui.viewmodel.SettingViewModel
import com.largeblueberry.resources.R as ResourceR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: SettingViewModel = hiltViewModel()
) {
    val authState by viewModel.authUiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SettingBackground)
    ) {
        // 상단 앱바
        TopAppBar(
            title = {
                Text(
                    text = stringResource(ResourceR.string.settingTitle),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(ResourceR.string.backButtonContentDescription),
                        tint = AppBlack
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppWhite
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            //  앱 정보 카드
            item {
                AppInfoCard()
            }

            //  계정 섹션
            item {
                AccountSection(
                    authState = authState,
                    onLoginClick = onNavigateToLogin,
                    onLogoutClick = { viewModel.signOut() }
                )
            }

            // ️ 앱 설정 섹션
            item {
                AppSettingsSection()
            }

            // ℹ 정보 섹션
            item {
                InfoSection()
            }

            //  앱 정보 섹션
            item {
                AboutSection()
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
            containerColor = AppWhite
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
                painter = painterResource(id = R.drawable.eareamsplash),
                contentDescription = stringResource(ResourceR.string.eareamLogoDescription),
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "버전 ${BuildConfig.VERSION_NAME}",
                fontSize = 12.sp,
                color = SettingUtilColor
            )
        }
    }
}

@Composable
private fun AccountSection(
    authState: AuthUiState,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    SettingSection(title = stringResource(ResourceR.string.account)) {
        when (authState) {
            is AuthUiState.Authenticated -> {
                // 로그인된 상태
                UserProfileItem(
                    user = authState.user,
                    onLogoutClick = onLogoutClick
                )
            }
            else -> {
                // 로그인되지 않은 상태
                SettingItem(
                    icon = Icons.Default.AccountCircle,
                    title = stringResource(ResourceR.string.login),
                    subtitle = stringResource(ResourceR.string.loginSubText),
                    onClick = onLoginClick,
                    showArrow = true
                )
            }
        }
    }
}

@Composable
private fun UserProfileItem(
    user: UserCore,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppWhite
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 이미지 또는 기본 아이콘
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SettingBasicUser, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.firstOrNull()?.toString() ?: "U",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppPrimaryBlue
                    )
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = AppBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(ResourceR.string.logout),
                    color = AppRed
                )
            }
        }
    }
}

@Composable
private fun AppSettingsSection() {
    SettingSection(title = stringResource(ResourceR.string.appSetting)) {
        SettingItem(
            icon = Icons.Default.Notifications,
            title = stringResource(ResourceR.string.appAlarmMainText),
            subtitle = stringResource(ResourceR.string.appAlarmSubText),
            onClick = { /* TODO: 알림 설정 */ }
        )

        SettingItem(
            icon = Icons.Default.Language,
            title = stringResource(ResourceR.string.languageMainText),
            subtitle = stringResource(ResourceR.string.languageSubText),
            onClick = { /* TODO: 언어 설정 */ }
        )

        SettingItem(
            icon = Icons.Default.Language,
            title = stringResource(ResourceR.string.themeMainText),
            subtitle = stringResource(ResourceR.string.themeSubText),
            onClick = { /* TODO: 테마 설정 */ }
        )
    }
}

@Composable
private fun InfoSection() {
    SettingSection(title = stringResource(ResourceR.string.appHelper)) {
        SettingItem(
            icon = Icons.AutoMirrored.Filled.Help,
            title = stringResource(ResourceR.string.appUseMainText),
            subtitle = stringResource(ResourceR.string.appUseSubText),
            onClick = { /* TODO: 사용법 */ }
        )


        SettingItem(
            icon = Icons.Default.BugReport,
            title = stringResource(ResourceR.string.appBugMainText),
            subtitle = stringResource(ResourceR.string.appBugSubText),
            onClick = { /* TODO: 버그 신고 */ }
        )
    }
}

@Composable
private fun AboutSection() {
    SettingSection(title = "정보") {
        SettingItem(
            icon = Icons.Default.Description,
            title = stringResource(ResourceR.string.serviceTermMainText),
            subtitle = stringResource(ResourceR.string.serviceTermSubText),
            onClick = { /* TODO: 서비스 약관 */ }
        )

        SettingItem(
            icon = Icons.AutoMirrored.Filled.ContactSupport,
            title = stringResource(ResourceR.string.aboutEareamMainText),
            subtitle = stringResource(ResourceR.string.aboutEareamSubText),
            onClick = { /* TODO: 서비스 소개 */ }
        )
    }
}
