package com.largeblueberry.aicompose.feature_auth.dataLayer.repository.impl

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
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

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                // Firebase에서 계정 삭제
                currentUser.delete().await()

                // 상태 업데이트
                _authState.value = null

                Log.i("AuthRepositoryImpl", "계정 삭제 성공")
                Result.success(Unit)
            } else {
                Log.e("AuthRepositoryImpl", "삭제할 사용자가 없습니다.")
                Result.failure(Exception("로그인된 사용자가 없습니다."))
            }
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "계정 삭제 실패", e)

            // 재인증이 필요한 경우 특별 처리
            if (e is FirebaseAuthRecentLoginRequiredException) {
                Log.w("AuthRepositoryImpl", "재인증이 필요합니다.")
                Result.failure(ReauthenticationRequiredException("계정 삭제를 위해 재인증이 필요합니다."))
            } else {
                Result.failure(e)
            }
        }
    }

    // 재인증 함수 추가
    override suspend fun reauthenticate(idToken: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                currentUser.reauthenticate(credential).await()

                Log.i("AuthRepositoryImpl", "재인증 성공")
                Result.success(Unit)
            } else {
                Log.e("AuthRepositoryImpl", "재인증할 사용자가 없습니다.")
                Result.failure(Exception("로그인된 사용자가 없습니다."))
            }
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "재인증 실패", e)
            Result.failure(e)
        }
    }
}

// 재인증이 필요한 경우를 나타내는 커스텀 예외
class ReauthenticationRequiredException(message: String) : Exception(message)