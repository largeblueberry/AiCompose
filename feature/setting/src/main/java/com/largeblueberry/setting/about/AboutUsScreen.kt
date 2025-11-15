package com.largeblueberry.setting.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largeblueberry.core_ui.LightCustomColors
import com.largeblueberry.resources.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(onNavigateBack: () -> Unit) {
    // 실제 버전 정보를 가져오는 로직이 있다면 여기에서 처리
    val appVersion = "1.0.0"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.about_us_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = LightCustomColors.titleBlueDark, // 파란색
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(id = R.string.app_slogan),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = LightCustomColors.subtitleBlue, // 연보라색
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp) // 긴 텍스트를 위한 패딩
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.app_description),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            SectionTitle(stringResource(id = R.string.section_title_team))
            TeamMember(
                role = stringResource(id = R.string.team_role_ai),
                description = stringResource(id = R.string.team_description_ai)
            )
            TeamMember(
                role = stringResource(id = R.string.team_role_server),
                description = stringResource(id = R.string.team_description_server)
            )
            TeamMember(
                role = stringResource(id = R.string.team_role_android),
                description = stringResource(id = R.string.team_description_android)
            )

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            SectionTitle(stringResource(id = R.string.section_title_license))
            Text(
                text = stringResource(id = R.string.license_description),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.contact_description),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(id = R.string.version_info, appVersion),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )
}

@Composable
private fun TeamMember(role: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(text = role, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
        Text(text = description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
    }
}

@Preview
@Composable
fun AboutUsScreenPreview() {
    AboutUsScreen(onNavigateBack = {})
}