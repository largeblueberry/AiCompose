package com.largeblueberry.feature_setting.ui.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.largeblueberry.ui.R
import com.largeblueberry.feature_setting.firebase.auth.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToMain: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val authState by viewModel.authState.collectAsState()

    // 🚀 Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    viewModel.signInWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                viewModel.clearError()
            }
        }
    }

    // 🔄 로그인 성공 시 설정 화면으로 돌아가기
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onNavigateToMain() // 설정 화면으로 돌아가기
        }
    }

    // 🚨 에러 메시지 표시
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
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

        // 🎵 앱 로고 및 제목 (업로드된 로고 사용)
        AppLogo()

        // 📱 설정에서 온 것을 나타내는 안내 텍스트
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

        // 🔐 로그인 카드
        LoginCard(
            isLoading = uiState.isLoading,
            canUseWithoutLogin = uiState.canUseWithoutLogin,
            usageWarning = viewModel.getUsageWarningMessage(),
            onGoogleSignIn = {
                // 수정된 부분: 함수 호출에서 속성 접근으로 변경
                val signInIntent = viewModel.googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            },
            onSkip = onNavigateBack // 건너뛰기로 설정 화면 복귀
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "로그인하면 서비스 약관에 동의하는 것으로 간주됩니다",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp)) // 고정 높이 공간

    }

    // 🔄 로딩 오버레이
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF4285F4)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "로그인 중...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun AppLogo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🎨 업로드된 로고 이미지 사용 (배경 제거)
        Image(
            painter = painterResource(id = R.drawable.eareamsplash),
            contentDescription = "이어름 로고",
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun LoginCard(
    isLoading: Boolean,
    canUseWithoutLogin: Boolean,
    usageWarning: String?,
    onGoogleSignIn: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🚨 사용량 경고 메시지
            usageWarning?.let { warning ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = warning,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE65100),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Google 로그인 버튼
            GoogleSignInButton(
                onClick = onGoogleSignIn,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 또는 구분선
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE0E0E0)
                )
                Text(
                    text = "  또는  ",
                    color = Color(0xFF999999),
                    fontSize = 12.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE0E0E0)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 건너뛰기 버튼 (설정 화면으로 돌아가기)
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(
                    text = "나중에 하기",
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}


@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White, // 흰색 배경
            contentColor = Color.Black, // 검은색 글자 (기본 텍스트 색상)
            disabledContainerColor = Color(0xFFE0E0E0), // 비활성화 시 연한 회색 배경
            disabledContentColor = Color(0xFF9E9E9E) // 비활성화 시 연한 회색 글자
        ),
        border = BorderStroke(1.dp, Color(0xFFDADCE0)), // 연한 회색 테두리
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp) // 로고 크기 조정 (구글 가이드라인 18-24dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Google 계정으로 로그인",
                fontSize = 15.sp, // 구글 가이드라인에 맞춰 폰트 크기 조정 (14-16sp)
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}