package com.largeblueberry.feature_setting.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.largeblueberry.feature_setting.firebase.auth.AuthResult
import com.largeblueberry.feature_setting.firebase.auth.GoogleAuthManager
import com.largeblueberry.feature_setting.firebase.auth.UsageLimitResult
import com.largeblueberry.feature_setting.firebase.firestore.UsageTracker
import com.largeblueberry.feature_setting.domain.repository.AuthRepository
import com.largeblueberry.feature_setting.firebase.firestore.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.Timestamp

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val googleAuthManager: GoogleAuthManager,
    private val usageTracker: UsageTracker,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun getCurrentUser(): FirebaseUser? = googleAuthManager.getCurrentUser()

    override fun isSignedIn(): Boolean = googleAuthManager.isSignedIn()

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        val result = googleAuthManager.signInWithGoogle(idToken)

        // 로그인 성공 시 사용자 정보를 Firestore에 저장
        if (result is AuthResult.Success) {
            saveUserToFirestore(result.user)
        }

        return result
    }

    override suspend fun signOut(): Result<Unit> = googleAuthManager.signOut()

    override suspend fun checkUsageLimit(userId: String): UsageLimitResult {
        return usageTracker.checkUsageLimit(userId)
    }

    override suspend fun incrementUsage(userId: String): Boolean {
        return usageTracker.incrementUsage(userId)
    }

    private suspend fun saveUserToFirestore(firebaseUser: FirebaseUser) {
        try {
            val user = User(
                id = firebaseUser.uid,                                    // ✅ uid → id
                name = firebaseUser.displayName ?: "Unknown User",       // ✅ displayName → name
                email = firebaseUser.email ?: "",                       // ✅ OK
                profilePictureUrl = firebaseUser.photoUrl?.toString(),   // ✅ photoUrl → profilePictureUrl
                isPremium = false,                                       // ✅ 기본값 설정
                createdAt = Timestamp.now(),                            // ✅ 생성 시간
                lastLoginAt = Timestamp.now()                           // ✅ 마지막 로그인
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

        } catch (e: Exception) {
            // 로그만 남기고 로그인은 성공으로 처리
            println("Failed to save user to Firestore: ${e.message}")
        }
    }
}