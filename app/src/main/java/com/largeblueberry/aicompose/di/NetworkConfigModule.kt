package com.largeblueberry.aicompose.di

import com.largeblueberry.aicompose.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkConfigModule {

    @Provides
    @Singleton
    @Named("base_url")
    fun provideBaseUrl(): String {
        return BuildConfig.BASE_URL
    }
}