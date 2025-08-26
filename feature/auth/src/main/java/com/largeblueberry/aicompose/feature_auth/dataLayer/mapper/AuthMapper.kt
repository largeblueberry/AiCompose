package com.largeblueberry.aicompose.feature_auth.dataLayer.mapper

import com.largeblueberry.aicompose.feature_auth.dataLayer.model.AuthResult
import com.largeblueberry.aicompose.feature_auth.domainLayer.model.AuthResultDomain

object AuthMapper {

    fun toDomain(authResult: AuthResult): AuthResultDomain {
        return when (authResult) {
            is AuthResult.Success -> {
                // 1. AuthResult.Success에서 받은 FirebaseUser를 데이터 레이어의 User 모델로 변환
                val userDataModel = UserMapper.toUser(authResult.user)
                // 2. 데이터 레이어의 User 모델을 도메인 레이어의 UserDomain 모델로 변환
                val userDomainModel = UserMapper.toDomain(userDataModel)
                // 3. 변환된 UserDomain을 사용하여 AuthResultDomain.Success 반환
                AuthResultDomain.Success(userDomainModel)
            }
            is AuthResult.Error -> {
                // AuthResult.Error의 메시지를 그대로 사용하여 AuthResultDomain.Error 반환
                AuthResultDomain.Error(authResult.message)
            }
        }
    }
}