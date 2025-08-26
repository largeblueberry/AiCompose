package com.largeblueberry.aicompose.feature_auth.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.aicompose.feature_auth.ui.model.AuthUiState
import com.largeblueberry.aicompose.feature_auth.ui.util.AppLogo
import com.largeblueberry.aicompose.feature_auth.ui.util.LoginCard
import kotlinx.coroutines.flow.collectLatest

const val TAG = "LoginScreen"

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    // ViewModel의 상태를 관찰합니다.
    val uiState = viewModel.uiState.collectAsState().value
    val authState = viewModel.authUiState.collectAsState().value

    // 1. Google Sign-In 결과를 처리할 ActivityResultLauncher 정의
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // ViewModel에 원본 Intent 데이터를 전달하여 처리하도록 합니다.
        viewModel.handleGoogleSignInResult(result.data)

        if (result.resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "Google Sign-In cancelled or failed with result code: ${result.resultCode}")
        }
    }

    // 2. ViewModel로부터 Google Sign-In Intent 요청을 관찰하고 실행합니다.
    LaunchedEffect(key1 = Unit) {
        viewModel.startGoogleSignInFlow.collectLatest { intent ->
            // ViewModel이 제공한 Intent를 실행합니다.
            googleSignInLauncher.launch(intent)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(Color(0xFFF8F9FA))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo()

        Text(
            text = "계정 연결",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Google 계정으로 로그인하여\n더 많은 기능을 이용해보세요",
            fontSize = 16.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        LoginCard(
            isLoading = uiState.isLoading, // ViewModel의 isLoading 상태 사용
            onGoogleSignIn = {
                // Google 로그인 버튼 클릭 시 ViewModel에 로그인 시작을 요청
                viewModel.onGoogleSignInClicked()
            },
            onSkip = onNavigateBack // 건너뛰기로 설정 화면 복귀
        )

        Text(
            text = "로그인하면 서비스 약관에 동의하는 것으로 간주됩니다",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp)) // 고정 높이 공간

        // ViewModel에서 발생한 에러 메시지 표시
        uiState.errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // 로그인 성공 시 화면 전환
        LaunchedEffect(authState) {
            if (authState is AuthUiState.Authenticated) {
                Log.d(TAG,"Login successful, navigating back.")
                onNavigateBack() // 로그인 성공 시 이전 화면으로 돌아갑니다.
            }
        }
    }
}
