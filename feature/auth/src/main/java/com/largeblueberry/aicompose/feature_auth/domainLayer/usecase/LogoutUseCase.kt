package com.largeblueberry.aicompose.feature_auth.domainLayer.usecase

import com.largeblueberry.aicompose.feature_auth.domainLayer.repository.AuthRepository
import javax.inject.Inject


class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut()
    }
}