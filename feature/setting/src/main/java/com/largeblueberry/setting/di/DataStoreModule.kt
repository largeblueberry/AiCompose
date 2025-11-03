package com.largeblueberry.setting.di // 더 공통적인 위치로 이동하는 것을 추천합니다.

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * 테마 설정을 위한 DataStore<Preferences> 인스턴스를 제공합니다.
     * @Named("theme") 어노테이션으로 구분됩니다.
     * 데이터는 "theme_pref" 파일에 저장됩니다.
     */
    @Provides
    @Singleton
    @Named("theme")
    fun provideThemeDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("theme_pref") }
        )
    }

    /**
     * 언어 설정을 위한 DataStore<Preferences> 인스턴스를 제공합니다.
     * @Named("language") 어노테이션으로 구분됩니다.
     * 데이터는 "language_pref" 파일에 저장됩니다.
     */
    @Provides
    @Singleton
    @Named("language")
    fun provideLanguageDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("language_pref") }
        )
    }
}