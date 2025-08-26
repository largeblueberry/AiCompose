package com.largeblueberry.feature_setting.firebase.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.largeblueberry.feature_setting.firebase.auth.GoogleAuthManager
import com.largeblueberry.feature_setting.firebase.firestore.UsageTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // GoogleAuthManager만 제공 (GoogleSignInClient는 내부에서 생성)
    @Provides
    @Singleton
    fun provideGoogleAuthManager(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth
    ): GoogleAuthManager {
        return GoogleAuthManager(context, firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideUsageTracker(
        firestore: FirebaseFirestore
    ): UsageTracker {
        return UsageTracker(firestore)
    }
}