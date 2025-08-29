package com.largeblueberry.aicompose.feature_auth.dataLayer.mapper

import com.largeblueberry.aicompose.feature_auth.dataLayer.model.AuthResultData
import com.largeblueberry.auth.model.AuthResult

object AuthMapper {

    fun toDomain(authResultData: AuthResultData): AuthResult {
        return when (authResultData) {
            is AuthResultData.Success -> {
                val userDomainModel = UserMapper.toDomain(authResultData.user)
                AuthResult.Success(userDomainModel)
            }
            is AuthResultData.Error -> {
                // AuthResult.Error의 메시지를 그대로 사용하여 AuthResultDomain.Error 반환
                AuthResult.Error(authResultData.message)
            }
        }
    }
}