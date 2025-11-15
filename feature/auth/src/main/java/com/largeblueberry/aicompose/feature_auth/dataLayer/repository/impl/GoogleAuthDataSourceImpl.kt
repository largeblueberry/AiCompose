package com.largeblueberry.aicompose.feature_auth.dataLayer.repository.impl

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.largeblueberry.aicompose.feature_auth.dataLayer.repository.GoogleAuthDataSource
import com.largeblueberry.auth.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext

private const val TAG = "GoogleSignInHelper"

class GoogleAuthDataSourceImpl(
    @ApplicationContext private val context: Context
): GoogleAuthDataSource {
    private val googleSignInClient by lazy {
        // GoogleSignInOptions 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    override fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    override fun getSignedInAccountFromIntent(data: Intent?): GoogleSignInAccount? {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            // GoogleSignIn 관련 API 예외 처리
            Log.e(TAG, "Google Sign-In API Exception: ${e.statusCode} - ${e.message}", e)

            when (e.statusCode) {
                CommonStatusCodes.SIGN_IN_REQUIRED -> {
                    // 사용자에게 로그인이 필요함을 알리거나 로그인 흐름을 다시 시작하도록 유도
                    Log.e(TAG, "Sign-in required. User needs to sign in again.")
                }
                CommonStatusCodes.NETWORK_ERROR -> {
                    // 네트워크 오류 발생 시 처리
                    Log.e(TAG, "Network error during Google Sign-In.")
                }
                // 그 외 다른 CommonStatusCodes에 대한 처리 추가 가능
                else -> {
                    Log.e(TAG, "Unhandled Google Sign-In API exception with status code: ${e.statusCode}")
                }
            }
            null // 예외 발생 시 null 반환
        } catch (e: Exception) {
            // 그 외 예상치 못한 모든 예외 처리
            Log.e(TAG, "Unexpected exception during Google Sign-In: ${e.message}", e)
            null // 예외 발생 시 null 반환
        }
    }

    // 재인증을 위한 Google Sign-In Intent 반환
    override fun getReauthenticationIntent(): Intent {
        // 재인증 시에는 기존 세션을 무시하고 새로 로그인하도록 함
        return googleSignInClient.signInIntent
    }

    // Google Sign-In 클라이언트에서 로그아웃
    override fun signOut(): Task<Void> {
        Log.d(TAG, "Signing out from Google Sign-In client")
        return googleSignInClient.signOut()
    }
}