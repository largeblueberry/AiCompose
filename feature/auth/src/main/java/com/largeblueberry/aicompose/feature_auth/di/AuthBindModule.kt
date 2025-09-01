package com.largeblueberry.aicompose.feature_auth.di

import com.largeblueberry.aicompose.feature_auth.dataLayer.repository.impl.AuthRepositoryImpl
import com.largeblueberry.auth.repository.AuthRepository
import com.largeblueberry.data.AuthGatewayImpl
import com.largeblueberry.domain.AuthGateway

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindsModule { // 추상 클래스

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindAuthGateway(
        authGatewayImpl: AuthGatewayImpl
    ): AuthGateway

    // 여기에 다른 @Binds 메서드만 추가합니다.
}