package com.largeblueberry.aicompose.library.di

import android.content.Context
import android.content.SharedPreferences
import com.largeblueberry.aicompose.feature_auth.dataLayer.repository.impl.AuthGatewayImpl
import com.largeblueberry.aicompose.library.dataLayer.repository.impl.UserUsageRepositoryImpl
import com.largeblueberry.usertracker.repository.AuthGateway
import com.largeblueberry.usertracker.repository.UserUsageRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserTrackerBindsModule {

    @Binds
    @Singleton
    abstract fun bindUserUsageRepository(
        userUsageRepositoryImpl: UserUsageRepositoryImpl
    ): UserUsageRepository

    companion object {
        @Provides
        @Singleton
        fun provideSharedPreferences(
            @ApplicationContext context: Context
        ): SharedPreferences {
            return context.getSharedPreferences("user_tracker_prefs", Context.MODE_PRIVATE)
        }
    }
}