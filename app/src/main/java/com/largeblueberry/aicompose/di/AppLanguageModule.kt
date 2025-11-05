package com.largeblueberry.aicompose.di

import com.largeblueberry.aicompose.ui.LanguageManager
import com.largeblueberry.setting.language.domain.LanguageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppLanguageModule {

    @Provides
    @Singleton
    fun provideLanguageManager(
        languageRepository: LanguageRepository
    ): LanguageManager {
        return LanguageManager(languageRepository)
    }
}