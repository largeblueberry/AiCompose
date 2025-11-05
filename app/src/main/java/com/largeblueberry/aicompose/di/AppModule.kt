package com.largeblueberry.aicompose.di

import com.largeblueberry.aicompose.ui.AppThemeDisplayNameProvider
import com.largeblueberry.core_ui.ThemeDisplayNameProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindThemeDisplayNameProvider(
        provider: AppThemeDisplayNameProvider
    ): ThemeDisplayNameProvider

}