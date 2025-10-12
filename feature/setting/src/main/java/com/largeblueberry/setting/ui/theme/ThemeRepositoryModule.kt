package com.largeblueberry.setting.ui.theme

import com.largeblueberry.setting.ui.theme.dataLayer.ThemeRepositoryImpl
import com.largeblueberry.setting.ui.theme.domain.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        themeRepositoryImpl: ThemeRepositoryImpl
    ): ThemeRepository
}