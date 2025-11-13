package com.largeblueberry.aicompose.feature_auth.dataLayer.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.largeblueberry.aicompose.feature_auth.dataLayer.mapper.AuthMapper
import com.largeblueberry.aicompose.feature_auth.dataLayer.mapper.UserMapper
import com.largeblueberry.aicompose.feature_auth.dataLayer.model.AuthResultData
import com.largeblueberry.auth.model.AuthResult
import com.largeblueberry.auth.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // Firebase Authentication 인스턴스를 주입받음
    private val authMapper: AuthMapper // AuthResult (Data) -> AuthResultDomain (Domain) 매핑을 위한 Mapper 주입
) : AuthRepository {

    override suspend fun signIn(idToken: String): AuthResult {
        return try {
            // Firebase 인증 로직: idToken을 사용하여 Firebase에 로그인
            val credential = GoogleAuthProvider.getCredential(idToken, null) // 토큰 타입에 따라 변경
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            // FirebaseUser를 dataLayer.model.User로 변환
            val dataUser = authResult.user?.let { UserMapper.toUser(it) }
                ?: throw IllegalStateException("Firebase user is null after successful sign-in.")

            // 변환된 dataUser를 사용하여 dataLayer.model.AuthResult.Success 생성
            val dataAuthResultData = AuthResultData.Success(dataUser)

            // Data Layer의 AuthResult를 Domain Layer의 AuthResultDomain으로 매핑
            authMapper.toDomain(dataAuthResultData)
        } catch (e: Exception) {
            val dataAuthResultData = AuthResultData.Error(e.localizedMessage ?: "알 수 없는 로그인 오류 발생")
            authMapper.toDomain(dataAuthResultData)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut() // Firebase 로그아웃
            Result.success(Unit) // 성공 시 Unit 반환
        } catch (e: Exception) {
            Result.failure(e) // 실패 시 예외 반환
        }
    }

    override suspend fun getCurrentUser(): com.largeblueberry.auth.model.UserCore? {
        // 1. Firebase에서 현재 사용자 정보를 가져옵니다.
        val firebaseUser = firebaseAuth.currentUser

        // 2. firebaseUser가 null이 아니면, Mapper를 사용해 Domain 모델(UserCore)로 변환하여 반환합니다.
        return firebaseUser?.let {
            // FirebaseUser -> data.User -> domain.UserCore
            val dataUser = UserMapper.toUser(it)
            UserMapper.toDomain(dataUser)
        }
    }
}