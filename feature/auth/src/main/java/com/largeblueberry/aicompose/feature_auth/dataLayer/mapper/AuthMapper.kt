package com.largeblueberry.aicompose.feature_auth.dataLayer.mapper

import com.largeblueberry.aicompose.feature_auth.dataLayer.model.AuthResult
import com.largeblueberry.aicompose.feature_auth.domainLayer.model.AuthResultDomain

object AuthMapper {

    fun toDomain(authResult: AuthResult): AuthResultDomain {
        return when (authResult) {
            is AuthResult.Success -> {
                val userDomainModel = UserMapper.toDomain(authResult.user)
                AuthResultDomain.Success(userDomainModel)
            }
            is AuthResult.Error -> {
                // AuthResult.Error의 메시지를 그대로 사용하여 AuthResultDomain.Error 반환
                AuthResultDomain.Error(authResult.message)
            }
        }
    }
}