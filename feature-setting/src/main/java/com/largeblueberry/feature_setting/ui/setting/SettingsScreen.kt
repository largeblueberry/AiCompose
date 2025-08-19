package com.largeblueberry.feature_setting.ui.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.ui.R
import com.largeblueberry.feature_setting.firebase.auth.AuthState
import com.largeblueberry.feature_setting.ui.login.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 🎯 상단 앱바
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
                        Icons.Default.ArrowBack,
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 🎵 앱 정보 카드
            item {
                AppInfoCard()
            }

            // 👤 계정 섹션
            item {
                AccountSection(
                    authState = authState,
                    onLoginClick = onNavigateToLogin,
                    onLogoutClick = { viewModel.signOut() }
                )
            }

            // ⚙️ 앱 설정 섹션
            item {
                AppSettingsSection()
            }

            // ℹ️ 정보 섹션
            item {
                InfoSection()
            }

            // 📱 앱 정보 섹션
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
                    .size(80.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "이어름",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Text(
                text = "듣고, 꿈꾸는 AI 작곡 서비스",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
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
    authState: AuthState,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    SettingsSection(title = "계정") {
        when (authState) {
            is AuthState.Authenticated -> {
                // 로그인된 상태
                UserProfileItem(
                    user = authState.user,
                    onLogoutClick = onLogoutClick
                )
            }
            else -> {
                // 로그인되지 않은 상태
                SettingsItem(
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
    user: com.google.firebase.auth.FirebaseUser,
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
                        text = user.displayName?.firstOrNull()?.toString() ?: "U",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName ?: "사용자",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = user.email ?: "",
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
    SettingsSection(title = "앱 설정") {
        SettingsItem(
            icon = Icons.Default.Notifications,
            title = "알림",
            subtitle = "푸시 알림 설정",
            onClick = { /* TODO: 알림 설정 */ }
        )

        SettingsItem(
            icon = Icons.Default.VolumeUp,
            title = "오디오 설정",
            subtitle = "음질 및 오디오 옵션",
            onClick = { /* TODO: 오디오 설정 */ }
        )

        SettingsItem(
            icon = Icons.Default.Storage,
            title = "저장소",
            subtitle = "파일 저장 위치 설정",
            onClick = { /* TODO: 저장소 설정 */ }
        )
    }
}

@Composable
private fun InfoSection() {
    SettingsSection(title = "도움말") {
        SettingsItem(
            icon = Icons.Default.Help,
            title = "사용법",
            subtitle = "앱 사용 가이드",
            onClick = { /* TODO: 사용법 */ }
        )

        SettingsItem(
            icon = Icons.Default.ContactSupport,
            title = "고객지원",
            subtitle = "문의 및 피드백",
            onClick = { /* TODO: 고객지원 */ }
        )

        SettingsItem(
            icon = Icons.Default.BugReport,
            title = "버그 신고",
            subtitle = "문제점 신고하기",
            onClick = { /* TODO: 버그 신고 */ }
        )
    }
}

@Composable
private fun AboutSection() {
    SettingsSection(title = "정보") {
        SettingsItem(
            icon = Icons.Default.Description,
            title = "서비스 약관",
            subtitle = "이용약관 및 정책",
            onClick = { /* TODO: 서비스 약관 */ }
        )

        SettingsItem(
            icon = Icons.Default.Security,
            title = "개인정보처리방침",
            subtitle = "개인정보 보호정책",
            onClick = { /* TODO: 개인정보처리방침 */ }
        )

        SettingsItem(
            icon = Icons.Default.Info,
            title = "앱 정보",
            subtitle = "버전 및 라이선스 정보",
            onClick = { /* TODO: 앱 정보 */ }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
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
            tint = Color(0xFF666666),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )

            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }

        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF999999),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen()
    }
}