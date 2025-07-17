package com.largeblueberry.feature_setting.domain.usecase

import com.largeblueberry.feature_setting.domain.repository.AuthRepository
import com.largeblueberry.feature_setting.firebase.auth.AuthResult
import com.largeblueberry.feature_setting.firebase.auth.UsageLimitResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AuthResult {
        return authRepository.signInWithGoogle(idToken)
    }
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut()
    }
}

class CheckUsageLimitUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userId: String): UsageLimitResult {
        return authRepository.checkUsageLimit(userId)
    }

    fun canUseWithoutLogin(): Boolean {
        // 비로그인 사용자는 제한 둘 예정
        return true
    }
}

class IncrementUsageUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userId: String): Boolean {
        return authRepository.incrementUsage(userId)
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.getCurrentUser()
}