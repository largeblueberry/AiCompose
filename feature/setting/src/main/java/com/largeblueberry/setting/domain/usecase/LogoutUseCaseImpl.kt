package com.largeblueberry.setting.domain.usecase

import com.largeblueberry.auth.repository.AuthRepository
import com.largeblueberry.auth.usecase.LogoutUseCase
import javax.inject.Inject

class LogoutUseCaseImpl @Inject constructor(
    private val authRepository: AuthRepository
) : LogoutUseCase {
    override suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut()
    }
}