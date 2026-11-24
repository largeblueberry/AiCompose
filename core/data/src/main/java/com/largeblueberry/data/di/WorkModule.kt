package com.largeblueberry.data.di

import android.content.Context
import androidx.work.WorkManager
import com.largeblueberry.data.transcription.WorkManagerTranscriptionScheduler
import com.largeblueberry.domain.repository.TranscriptionScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 백그라운드 작업. WorkManager는 앱이 주는 설정(HiltWorkerFactory)으로 처음 쓸 때 초기화된다
 * (MainApplication의 Configuration.Provider, 매니페스트에서 기본 초기화 제거).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WorkModule {

    @Binds
    @Singleton
    abstract fun bindTranscriptionScheduler(impl: WorkManagerTranscriptionScheduler): TranscriptionScheduler

    companion object {
        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
    }
}
