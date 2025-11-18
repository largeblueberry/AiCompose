package com.largeblueberry.setting.serviceterm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.largeblueberry.navigation.SettingsNavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceTermScreen(
    // 실제 환경에서는 onNavigateToDetail 함수가 TermType을 인자로 받아 상세 화면으로 이동시킵니다.
    navigationActions : SettingsNavigationActions = SettingsNavigationActions(),
    onNavigateToDetail: (TermType) -> Unit // 상세 화면으로 이동하는 콜백 함수
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("법적 고지 및 약관") },
                navigationIcon = {
                    IconButton(onClick = navigationActions.onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                }
            )
        }
    ){ paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 1. 서비스 이용약관 클릭 시 상세 화면으로 이동
            TermLinkItem(
                title = TermType.TermsOfService.title,
                subtitle = "Service Terms and Conditions",
                onClick = { onNavigateToDetail(TermType.TermsOfService) }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // 2. 개인정보 처리방침 클릭 시 상세 화면으로 이동
            TermLinkItem(
                title = TermType.PrivacyPolicy.title,
                subtitle = "Privacy Policy",
                onClick = { onNavigateToDetail(TermType.PrivacyPolicy) }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun TermLinkItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}