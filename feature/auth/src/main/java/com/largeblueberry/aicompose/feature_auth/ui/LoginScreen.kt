package com.largeblueberry.aicompose.feature_auth.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.largeblueberry.auth.model.AuthUiState
import com.largeblueberry.aicompose.feature_auth.ui.util.AppLogo
import com.largeblueberry.aicompose.feature_auth.ui.util.LoginCard
import kotlinx.coroutines.flow.collectLatest
import com.largeblueberry.resources.R as ResourceR

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    // ViewModel의 상태를 관찰
    val uiState = viewModel.uiState.collectAsState().value
    val authState = viewModel.authUiState.collectAsState().value

    // Google Sign-In 결과를 처리할 ActivityResultLauncher 정의
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    // ViewModel로부터 Google Sign-In Intent 요청을 관찰하고 실행
    LaunchedEffect(key1 = Unit) {
        viewModel.startGoogleSignInFlow.collectLatest { intent ->
            googleSignInLauncher.launch(intent)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            // 1. 배경색을 테마의 background 색상으로 변경
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AppLogo()

        Text(
            text = stringResource(ResourceR.string.accountSync),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            // 2. 메인 텍스트 색상을 테마의 onBackground 색상으로 변경
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(ResourceR.string.googleLoginExperienceMore),
            fontSize = 16.sp,
            // 3. 보조 텍스트 색상을 onSurfaceVariant로 변경하여 시각적 계층 구분
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // LoginCard는 내부적으로 테마를 따르도록 수정이 필요합니다.
        LoginCard(
            isLoading = uiState.isLoading,
            onGoogleSignIn = {
                viewModel.onGoogleSignInClicked()
            },
            onSkip = onNavigateBack
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(ResourceR.string.loginTermsAgreementNotice),
            style = MaterialTheme.typography.bodySmall,
            // 3. 약관 안내와 같은 텍스트도 onSurfaceVariant로 변경
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 에러 메시지 (이미 MaterialTheme 색상을 잘 사용하고 있음)
        uiState.errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // 로그인 성공 시 화면 전환 (기존과 동일)
        LaunchedEffect(authState) {
            if (authState is AuthUiState.Authenticated) {
                onNavigateBack()
            }
        }
    }
}