package com.largeblueberry.setting.language.di

import com.largeblueberry.setting.language.data.LanguageRepositoryImpl
import com.largeblueberry.setting.language.domain.LanguageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LanguageRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLanguageRepository(
        languageRepositoryImpl: LanguageRepositoryImpl
    ): LanguageRepository
}