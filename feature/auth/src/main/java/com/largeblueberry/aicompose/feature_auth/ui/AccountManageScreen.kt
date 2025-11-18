package com.largeblueberry.aicompose.feature_auth.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.aicompose.feature_auth.ui.util.AccountManagementMenu
import com.largeblueberry.aicompose.feature_auth.ui.util.LoginCard
import com.largeblueberry.aicompose.feature_auth.ui.util.ReauthenticationErrorDialog
import com.largeblueberry.aicompose.feature_auth.ui.util.ReauthenticationLoadingDialog
import com.largeblueberry.aicompose.feature_auth.ui.util.ReauthenticationRequiredDialog
import com.largeblueberry.auth.model.AuthUiState
import com.largeblueberry.auth.model.LoginUiState
import com.largeblueberry.auth.model.UserCore
import kotlinx.coroutines.flow.collectLatest
import com.largeblueberry.resources.R as ResourceR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManageScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val uiState = viewModel.uiState.collectAsState().value
    val authState = viewModel.authUiState.collectAsState().value
    val reauthState = viewModel.reauthenticationState.collectAsState().value

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Google Sign-In 런처 (일반 로그인용)
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    // Google Sign-In 런처 (재인증용)
    val reauthenticationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleReauthenticationResult(result.data)
    }

    // 일반 로그인 플로우
    LaunchedEffect(key1 = Unit) {
        viewModel.startGoogleSignInFlow.collectLatest { intent ->
            googleSignInLauncher.launch(intent)
        }
    }

    // 재인증 플로우
    LaunchedEffect(key1 = Unit) {
        viewModel.startReauthenticationFlow.collectLatest { intent ->
            reauthenticationLauncher.launch(intent)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (authState) {
                            is AuthUiState.Authenticated -> "계정 관리"
                            else -> "로그인"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (authState is AuthUiState.Authenticated) Arrangement.Top else Arrangement.Center
        ) {
            when (authState) {
                is AuthUiState.Authenticated -> {
                    // 로그인된 상태 - 완전한 계정 관리 화면
                    AccountManagementContent(
                        user = authState.user,
                        isLoading = uiState.isLoading,
                        onSignOut = { viewModel.signOut() },
                        onDeleteAccount = { showDeleteDialog = true }
                    )
                }
                is AuthUiState.NotAuthenticated, is AuthUiState.Error -> {
                    // 로그인 화면
                    LoginContent(
                        uiState = uiState,
                        onGoogleSignIn = { viewModel.onGoogleSignInClicked() },
                        onSkip = onNavigateBack
                    )
                }
                AuthUiState.Loading -> {
                    LoadingContent()
                }
            }
        }
    }

    // 회원 탈퇴 다이얼로그
    if (showDeleteDialog) {
        DeleteAccountDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteAccount()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // 재인증 관련 다이얼로그들
    when (reauthState) {
        is ReauthenticationState.Required -> {
            ReauthenticationRequiredDialog(
                onConfirm = { viewModel.startReauthentication() },
                onDismiss = { viewModel.clearReauthenticationState() }
            )
        }
        is ReauthenticationState.Loading -> {
            ReauthenticationLoadingDialog()
        }
        is ReauthenticationState.Success -> {
            LaunchedEffect(reauthState) {
                // 성공 시 자동으로 상태 클리어 (ViewModel에서 자동 처리됨)
            }
        }
        is ReauthenticationState.Error -> {
            ReauthenticationErrorDialog(
                errorMessage = reauthState.message,
                onRetry = { viewModel.startReauthentication() },
                onDismiss = { viewModel.clearReauthenticationState() }
            )
        }
        ReauthenticationState.None -> {
            // 아무것도 표시하지 않음
        }
    }
}

@Composable
fun AccountManagementContent(
    user: UserCore,
    isLoading: Boolean,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 프로필 섹션
        ProfileSection(user = user)

        Spacer(modifier = Modifier.height(48.dp))

        // 계정 관리 메뉴들
        AccountManagementMenu(
            isLoading = isLoading,
            onSignOut = onSignOut,
            onDeleteAccount = onDeleteAccount
        )
    }
}

@Composable
private fun ProfileSection(user: UserCore) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 프로필 이미지/아바타
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.firstOrNull()?.toString()?.uppercase() ?: "U",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onGoogleSignIn: () -> Unit,
    onSkip: () -> Unit
) {
    // 상단 타이틀 영역
    Text(
        text = stringResource(ResourceR.string.accountSync),
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 24.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(ResourceR.string.googleLoginExperienceMore),
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 24.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(48.dp))

    LoginCard(
        isLoading = uiState.isLoading,
        onGoogleSignIn = onGoogleSignIn,
        onSkip = onSkip
    )

    Spacer(modifier = Modifier.height(48.dp))

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

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(ResourceR.string.loading_login),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(ResourceR.string.delete_account_title)) },
        text = {
            Text(
                stringResource(ResourceR.string.delete_account_message)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(ResourceR.string.delete_account_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(ResourceR.string.reauth_cancel)) // 기존 '취소' 리소스 재사용
            }
        }
    )
}