package com.largeblueberry.aicompose.feature_auth.domainLayer.usecase

import com.largeblueberry.aicompose.feature_auth.domainLayer.model.AuthResultDomain
import com.largeblueberry.aicompose.feature_auth.domainLayer.repository.AuthRepository
import javax.inject.Inject


class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(idToken: String): AuthResultDomain {
        return authRepository.signIn(idToken)
    }
}