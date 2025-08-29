package com.largeblueberry.aicompose.feature_auth.domainLayer.usecase

import com.largeblueberry.auth.model.AuthResult
import com.largeblueberry.auth.repository.AuthRepository
import javax.inject.Inject


class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(idToken: String): AuthResult {
        return authRepository.signIn(idToken)
    }
}