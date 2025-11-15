package com.largeblueberry.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.auth.model.AuthUiState
import com.largeblueberry.auth.model.UserCore
import com.largeblueberry.core_ui.AppBlack
import com.largeblueberry.core_ui.AppPrimaryBlue
import com.largeblueberry.core_ui.AppRed
import com.largeblueberry.core_ui.AppWhite
import com.largeblueberry.core_ui.LightCustomColors
import com.largeblueberry.core_ui.SettingBasicUser
import com.largeblueberry.core_ui.customColors
import com.largeblueberry.navigation.SettingsNavigationActions
import com.largeblueberry.ui.R
import com.largeblueberry.setting.util.SettingItem
import com.largeblueberry.setting.util.SettingSection
import com.largeblueberry.setting.viewmodel.SettingViewModel
import com.largeblueberry.resources.R as ResourceR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navigationActions : SettingsNavigationActions = SettingsNavigationActions(),
    viewModel: SettingViewModel = hiltViewModel()
) {
    val authState by viewModel.authUiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.customColors.settingBackground)
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
                IconButton(onClick = navigationActions.onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(ResourceR.string.backButtonContentDescription)
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
                    onLoginClick = navigationActions.onNavigateToLogin,
                    onLogoutClick = { viewModel.signOut() }
                )
            }

            // ️ 앱 설정 섹션
            item {
                AppSettingsSection(navigationActions = navigationActions)
            }

            //  앱 정보 섹션
            item {
                AboutSection(navigationActions = navigationActions)
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
                color = LightCustomColors.titleBlueDark, // 파란색
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 부제목
            Text(
                text = stringResource(id = ResourceR.string.app_subtitle),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = LightCustomColors.subtitleBlue, // 연보라색
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp) // 긴 텍스트를 위한 패딩
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
fun AppSettingsSection(
    navigationActions : SettingsNavigationActions = SettingsNavigationActions()
) {
    SettingSection(title = stringResource(ResourceR.string.appSetting)) {

        SettingItem(
            icon = Icons.Default.Language,
            title = stringResource(ResourceR.string.languageMainText),
            subtitle = stringResource(ResourceR.string.languageSubText),
            onClick = navigationActions.onNavigateToLanguage
        )

        SettingItem(
            icon = Icons.Default.Palette,
            title = stringResource(ResourceR.string.themeMainText),
            subtitle = stringResource(ResourceR.string.themeSubText),
            onClick = navigationActions.onNavigateToTheme
        )
    }
}

@Composable
private fun AboutSection(
    navigationActions : SettingsNavigationActions = SettingsNavigationActions()
) {
    SettingSection(title = "정보") {
        SettingItem(
            icon = Icons.Default.Description,
            title = stringResource(ResourceR.string.serviceTermMainText),
            subtitle = stringResource(ResourceR.string.serviceTermSubText),
            onClick = navigationActions.onNavigateToServiceTerm
        )

        SettingItem(
            icon = Icons.AutoMirrored.Filled.ContactSupport,
            title = stringResource(ResourceR.string.aboutEareamMainText),
            subtitle = stringResource(ResourceR.string.aboutEareamSubText),
            onClick = navigationActions.onNavigateToAbout
        )
    }
}
