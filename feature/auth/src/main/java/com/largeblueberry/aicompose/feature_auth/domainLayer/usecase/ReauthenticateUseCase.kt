package com.largeblueberry.aicompose.feature_auth.domainLayer.usecase

import android.util.Log
import com.largeblueberry.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * 사용자 재인증을 처리하는 UseCase
 *
 * 주요 기능:
 * - Google ID 토큰으로 현재 사용자 재인증
 * - 계정 삭제 등 민감한 작업 전 재인증 수행
 * - Firebase Authentication의 재인증 요구사항 충족
 */
class ReauthenticateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    private companion object {
        private const val TAG = "ReauthenticateUseCase"
    }

    /**
     * Google ID 토큰으로 현재 사용자를 재인증합니다.
     *
     * @param idToken Google Sign-In에서 획득한 ID 토큰
     * @return Result<Unit> - 재인증 성공 시 success, 실패 시 failure
     *
     * 사용 시나리오:
     * 1. 계정 삭제 전 재인증
     * 2. 비밀번호 변경 전 재인증
     * 3. 이메일 변경 전 재인증
     * 4. 기타 민감한 작업 전 재인증
     */
    suspend operator fun invoke(idToken: String): Result<Unit> {
        return try {
            Log.d(TAG, "Starting reauthentication process")

            // 입력 검증
            if (idToken.isBlank()) {
                Log.e(TAG, "ID token is blank")
                return Result.failure(IllegalArgumentException("ID 토큰이 비어있습니다."))
            }

            // AuthRepository를 통해 재인증 수행
            val result = authRepository.reauthenticate(idToken)

            result.fold(
                onSuccess = {
                    Log.i(TAG, "Reauthentication completed successfully")
                    Result.success(Unit)
                },
                onFailure = { exception ->
                    Log.e(TAG, "Reauthentication failed", exception)
                    Result.failure(exception)
                }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during reauthentication", e)
            Result.failure(e)
        }
    }
}