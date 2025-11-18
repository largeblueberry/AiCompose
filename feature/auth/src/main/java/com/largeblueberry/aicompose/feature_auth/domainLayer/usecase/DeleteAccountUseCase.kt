package com.largeblueberry.aicompose.feature_auth.domainLayer.usecase

import com.largeblueberry.auth.repository.AuthRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.deleteAccount()
    }
}