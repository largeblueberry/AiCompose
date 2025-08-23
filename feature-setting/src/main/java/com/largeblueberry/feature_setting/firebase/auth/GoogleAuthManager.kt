package com.largeblueberry.feature_setting.firebase.auth

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.largeblueberry.feature_setting.BuildConfig

@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {

    val googleSignInClient: GoogleSignInClient by lazy {
        Log.d("AuthDebug", "Initializing googleSignInClient. GOOGLE_CLIENT_ID: ${BuildConfig.GOOGLE_CLIENT_ID}")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)

    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            authResult.user?.let { user ->
                AuthResult.Success(user)
            } ?: AuthResult.Error("로그인에 실패했습니다.")

        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            googleSignInClient.signOut().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isSignedIn(): Boolean = getCurrentUser() != null
}