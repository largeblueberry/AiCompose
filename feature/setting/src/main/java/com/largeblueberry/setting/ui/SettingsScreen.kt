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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.aicompose.feature_auth.domainLayer.model.UserDomain
import com.largeblueberry.ui.R
import com.largeblueberry.aicompose.feature_auth.ui.LoginViewModel
import com.largeblueberry.aicompose.feature_auth.ui.model.AuthUiState
import com.largeblueberry.setting.ui.util.SettingItem
import com.largeblueberry.setting.ui.util.SettingSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val authState by viewModel.authUiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 상단 앱바
        TopAppBar(
            title = {
                Text(
                    text = "설정",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color(0xFF333333)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
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
            containerColor = Color.White
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
                contentDescription = "이어름 로고",
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "버전 1.0.0",
                fontSize = 12.sp,
                color = Color(0xFF999999)
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
    SettingSection(title = "계정") {
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
                    title = "로그인",
                    subtitle = "Google 계정으로 로그인하세요",
                    onClick = onLoginClick,
                    showArrow = true
                )
            }
        }
    }
}

@Composable
private fun UserProfileItem(
    user: UserDomain,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3F8FF)
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
                        .background(Color(0xFF4285F4), CircleShape),
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
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "로그아웃",
                    color = Color(0xFFE53E3E)
                )
            }
        }
    }
}

@Composable
private fun AppSettingsSection() {
    SettingSection(title = "앱 설정") {
        SettingItem(
            icon = Icons.Default.Notifications,
            title = "알림",
            subtitle = "푸시 알림 설정",
            onClick = { /* TODO: 알림 설정 */ }
        )

        SettingItem(
            icon = Icons.Default.Language,
            title = "언어",
            subtitle = "언어 설정",
            onClick = { /* TODO: 언어 설정 */ }
        )

        SettingItem(
            icon = Icons.Default.Storage,
            title = "저장소",
            subtitle = "파일 저장 위치 설정",
            onClick = { /* TODO: 저장소 설정 */ }
        )
    }
}

@Composable
private fun InfoSection() {
    SettingSection(title = "도움말") {
        SettingItem(
            icon = Icons.AutoMirrored.Filled.Help,
            title = "사용법",
            subtitle = "앱 사용 가이드",
            onClick = { /* TODO: 사용법 */ }
        )

        SettingItem(
            icon = Icons.AutoMirrored.Filled.ContactSupport,
            title = "About eaream",
            subtitle = "서비스 소개",
            onClick = { /* TODO: 서비스 소개 */ }
        )

        SettingItem(
            icon = Icons.Default.BugReport,
            title = "버그 신고",
            subtitle = "문제점 신고하기",
            onClick = { /* TODO: 버그 신고 */ }
        )
    }
}

@Composable
private fun AboutSection() {
    SettingSection(title = "정보") {
        SettingItem(
            icon = Icons.Default.Description,
            title = "서비스 약관",
            subtitle = "이용약관 및 정책",
            onClick = { /* TODO: 서비스 약관 */ }
        )

        SettingItem(
            icon = Icons.Default.Security,
            title = "개인정보처리방침",
            subtitle = "개인정보 보호정책",
            onClick = { /* TODO: 개인정보처리방침 */ }
        )

        SettingItem(
            icon = Icons.Default.Info,
            title = "앱 정보",
            subtitle = "버전 및 라이선스 정보",
            onClick = { /* TODO: 앱 정보 */ }
        )
    }
}
