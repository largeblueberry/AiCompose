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
    @Singleton // AuthMapper가 싱글톤이므로, Hilt도 싱글톤으로 제공하도록 @Singleton 어노테이션을 붙입니다.
    fun provideAuthMapper(): AuthMapper {
        return AuthMapper // Kotlin object는 그 자체로 인스턴스이므로, 객체 이름을 반환합니다.
    }

    // 만약 UserMapper도 object이고, Hilt를 통해 주입되어야 한다면 유사하게 추가합니다.
    @Provides
    @Singleton
    fun provideUserMapper(): UserMapper {
        return UserMapper
    }
}