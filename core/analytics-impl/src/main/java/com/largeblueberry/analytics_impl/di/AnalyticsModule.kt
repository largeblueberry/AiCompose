package com.largeblueberry.analytics_impl.di

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import com.largeblueberry.analytics_impl.FirebaseAnalyticsHelper
import com.largeblueberry.analyticshelper.AnalyticsHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    // 1. FirebaseAnalytics 인스턴스를 제공하는 방법을 Hilt에 알려줌
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        // Firebase.analytics는 내부적으로 context를 사용합니다.
        return Firebase.analytics
    }

    // 2. FirebaseCrashlytics 인스턴스를 제공하는 방법을 Hilt에 알려줌
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return Firebase.crashlytics
    }

    // 3. 위 두 인스턴스를 사용하여 AnalyticsHelper를 제공하는 방법을 Hilt에 알려줌
    @Provides
    @Singleton
    fun provideAnalyticsHelper(
        firebaseAnalytics: FirebaseAnalytics,
        firebaseCrashlytics: FirebaseCrashlytics
    ): AnalyticsHelper {
        // ✨ 중요: 반환 타입이 인터페이스(AnalyticsHelper)입니다.
        // 이렇게 해야 다른 모듈에서 구현체가 아닌 인터페이스에만 의존할 수 있습니다.
        return FirebaseAnalyticsHelper(
            firebaseAnalytics = firebaseAnalytics,
            firebaseCrashlytics = firebaseCrashlytics
        )
    }
}