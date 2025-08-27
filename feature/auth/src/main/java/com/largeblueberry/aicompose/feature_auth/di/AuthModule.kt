package com.largeblueberry.aicompose.feature_auth.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.largeblueberry.aicompose.feature_auth.dataLayer.repository.GoogleAuthDataSource
import com.largeblueberry.aicompose.feature_auth.dataLayer.repository.impl.GoogleAuthDataSourceImpl
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

    // GoogleAuthDataSource 제공
    @Provides
    @Singleton
    fun provideGoogleAuthDataSource(@ApplicationContext context: Context): GoogleAuthDataSource {
        return GoogleAuthDataSourceImpl(context)
    }

}
