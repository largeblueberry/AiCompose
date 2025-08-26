package com.largeblueberry.aicompose.feature_auth.di

import com.largeblueberry.aicompose.feature_auth.dataLayer.repository.AuthRepositoryImpl
import com.largeblueberry.aicompose.feature_auth.domainLayer.repository.AuthRepository
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

    // 여기에 다른 @Binds 메서드만 추가합니다.
}