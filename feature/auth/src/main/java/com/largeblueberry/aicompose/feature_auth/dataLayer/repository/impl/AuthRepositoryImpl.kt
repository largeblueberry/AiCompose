package com.largeblueberry.aicompose.feature_auth.dataLayer.repository.impl

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.largeblueberry.aicompose.feature_auth.dataLayer.mapper.AuthMapper
import com.largeblueberry.aicompose.feature_auth.dataLayer.mapper.UserMapper
import com.largeblueberry.aicompose.feature_auth.dataLayer.model.AuthResultData
import com.largeblueberry.auth.model.AuthResult
import com.largeblueberry.auth.model.UserCore
import com.largeblueberry.auth.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val authMapper: AuthMapper
) : AuthRepository {

    private val _authState = MutableStateFlow<UserCore?>(null)
    override val authState: StateFlow<UserCore?> = _authState.asStateFlow()

    init {
        // Firebase Auth 상태 변화 리스너 등록
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val userCore = firebaseUser?.let {
                val dataUser = UserMapper.toUser(it)
                UserMapper.toDomain(dataUser)
            }
            _authState.value = userCore
            Log.d("AuthRepositoryImpl", "Auth state changed: ${userCore?.name ?: "null"}")
        }
    }

    override suspend fun signIn(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            val dataUser = authResult.user?.let { UserMapper.toUser(it) }
                ?: throw IllegalStateException("Firebase user is null after successful sign-in.")

            val dataAuthResultData = AuthResultData.Success(dataUser)

            // 로그인 성공 시 상태 업데이트 (AuthStateListener에서도 호출되지만 명시적으로)
            val userCore = UserMapper.toDomain(dataUser)
            _authState.value = userCore

            authMapper.toDomain(dataAuthResultData)
        } catch (e: Exception) {
            val dataAuthResultData = AuthResultData.Error(e.localizedMessage ?: "알 수 없는 로그인 오류 발생")
            authMapper.toDomain(dataAuthResultData)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            // 로그아웃 시 상태 업데이트 (AuthStateListener에서도 호출되지만 명시적으로)
            _authState.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): UserCore? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let {
            val dataUser = UserMapper.toUser(it)
            UserMapper.toDomain(dataUser)
        }
    }

    override suspend fun signInAnonymously(): Result<String> {
        return try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val userId = authResult.user?.uid

            if (userId != null) {
                Log.i("AuthRepositoryImpl", "익명 인증 성공: $userId")

                // 익명 로그인 성공 시 상태 업데이트
                val firebaseUser = authResult.user
                val userCore = firebaseUser?.let {
                    val dataUser = UserMapper.toUser(it)
                    UserMapper.toDomain(dataUser)
                }
                _authState.value = userCore

                Result.success(userId)
            } else {
                Log.e("AuthRepositoryImpl", "익명 인증 후 UID가 null입니다.")
                Result.failure(Exception("익명 인증 후 UID가 null입니다."))
            }
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "익명 인증 실패", e)
            Result.failure(e)
        }
    }
}