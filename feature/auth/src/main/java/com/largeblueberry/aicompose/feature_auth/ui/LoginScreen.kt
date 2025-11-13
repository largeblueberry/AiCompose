package com.largeblueberry.aicompose.feature_auth.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.aicompose.feature_auth.ui.util.LoginCard
import com.largeblueberry.auth.model.AuthUiState
import kotlinx.coroutines.flow.collectLatest
import com.largeblueberry.resources.R as ResourceR

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsState().value
    val authState = viewModel.authUiState.collectAsState().value

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.startGoogleSignInFlow.collectLatest { intent ->
            googleSignInLauncher.launch(intent)
        }
    }

    // Box를 사용해서 전체 화면을 감싸고 중앙 정렬
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center // 👈 핵심: 전체 콘텐츠를 중앙에 배치
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // 👈 세로 중앙 정렬 추가
        ) {

            // 상단 타이틀 영역
            Text(
                text = stringResource(ResourceR.string.accountSync),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(ResourceR.string.googleLoginExperienceMore),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp) // 좌우 패딩 추가
            )

            Spacer(modifier = Modifier.height(48.dp)) // 간격을 좀 더 늘림

            // 메인 콘텐츠 영역 (로그인 상태에 따라 다르게 표시)
            when (authState) {
                is AuthUiState.Authenticated -> {
                    // 로그인된 상태: 환영 메시지와 로그아웃 버튼 표시
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "로그인되었습니다! 🎉",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.signOut() }) {
                            Text(text = "로그아웃")
                        }
                    }
                }
                is AuthUiState.NotAuthenticated, is AuthUiState.Error -> {
                    // 로그아웃된 상태: 기존 LoginCard 사용
                    LoginCard(
                        isLoading = uiState.isLoading,
                        onGoogleSignIn = {
                            viewModel.onGoogleSignInClicked()
                        },
                        onSkip = onNavigateBack
                    )
                }
                AuthUiState.Loading -> {
                    // 로딩 중: 로딩 인디케이터 표시
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "로그인 중...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp)) // 간격을 좀 더 늘림

            // 하단 약관 동의 안내
            Text(
                text = stringResource(ResourceR.string.loginTermsAgreementNotice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            // 에러 메시지 표시
            uiState.errorMessage?.let { errorMessage ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}