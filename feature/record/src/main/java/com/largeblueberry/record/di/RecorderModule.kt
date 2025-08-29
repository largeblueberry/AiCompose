package com.largeblueberry.record.di

import com.largeblueberry.record.dataLayer.repository.impl.RecordRepositoryImpl // import 경로 변경
import com.largeblueberry.record.domain.repository.RecordRepository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecorderModule {

    //인터페이스이므로 binds
    @Binds
    @Singleton
    abstract fun bindAudioRecorder(
        audioRecorderImpl: RecordRepositoryImpl // 매개변수 타입 변경
    ): RecordRepository
}
