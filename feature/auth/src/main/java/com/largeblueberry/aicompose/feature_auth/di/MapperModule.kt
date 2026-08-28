package com.largeblueberry.aicompose.feature_auth.di

import com.largeblueberry.aicompose.feature_auth.dataLayer.mapper.AuthMapper
import com.largeblueberry.aicompose.feature_auth.dataLayer.mapper.UserMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {
    @Provides
    @Singleton // AuthMapper가 싱글톤
    fun provideAuthMapper(): AuthMapper {
        return AuthMapper
    }

    @Provides
    @Singleton
    fun provideUserMapper(): UserMapper {
        return UserMapper
    }
}