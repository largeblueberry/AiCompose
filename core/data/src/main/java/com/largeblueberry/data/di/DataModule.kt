package com.largeblueberry.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.largeblueberry.data.repository.LanguageRepositoryImpl
import com.largeblueberry.domain.repository.LanguageRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences

// 3. 수정됨: Context의 확장 프로퍼티로 DataStore 인스턴스 생성
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// 4. 삭제됨: 불필요한 빈 함수 선언을 제거합니다.
// private fun preferencesDataStore(name: String): ReadOnlyProperty<Context, DataStore<Preferences>> {}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindLanguageRepository(languageRepositoryImpl: LanguageRepositoryImpl): LanguageRepository

    companion object {
        @Provides
        @Singleton
        fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            // Context 확장 프로퍼티를 통해 싱글톤 인스턴스를 반환
            return context.dataStore
        }
    }
}
