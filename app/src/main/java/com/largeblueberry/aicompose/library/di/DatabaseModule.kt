package com.largeblueberry.aicompose.library.di

import android.content.Context
import androidx.room.Room
import com.largeblueberry.aicompose.data.record.local.AudioDatabase
import com.largeblueberry.aicompose.data.record.local.AudioRecordDao
import com.largeblueberry.aicompose.library.domainLayer.repository.LibraryRepository
import com.largeblueberry.aicompose.library.dataLayer.repository.impl.LibraryRepositoryImpl
import com.largeblueberry.aicompose.library.domainLayer.usecase.DeleteAudioRecordUseCase
import com.largeblueberry.aicompose.library.domainLayer.usecase.GetAudioRecordsUseCase
import com.largeblueberry.aicompose.library.domainLayer.usecase.RenameAudioRecordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // 1. AudioDatabase 인스턴스를 만드는 방법 정의
    @Provides
    @Singleton // 앱 전체에서 딱 한 번만 생성
    fun provideAudioDatabase(@ApplicationContext context: Context): AudioDatabase {
        return Room.databaseBuilder(
            context,
            AudioDatabase::class.java,
            "audio_database_v2"
        ).build()
    }

    // 2. AudioRecordDao 를 만드는 방법 정의
    // -> 위에서 만든 AudioDatabase가 있어야만 만들 수 있다고 알려줌
    // bind가 아닌 이유 -> 인터페이스지만, room에 의해 구현체가 자동 생성되기 때문
    @Provides
    fun provideAudioRecordDao(database: AudioDatabase): AudioRecordDao {
        return database.audioRecordDao()
    }

    // 3. AudioRecordRepository 를 만드는 방법 정의
    // -> AudioRecordDao가 있어야만 만들 수 있다고 알려줌
    @Provides
    @Singleton
    fun provideAudioRecordRepository(dao: AudioRecordDao): LibraryRepository {

        return LibraryRepositoryImpl(dao)
    }


    // 4. UseCase들을 만드는 방법 정의
    @Provides
    fun provideDeleteAudioRecordUseCase(repository: LibraryRepository): DeleteAudioRecordUseCase {
        return DeleteAudioRecordUseCase(repository)
    }

    @Provides
    fun provideGetAudioRecordsUseCase(repository: LibraryRepository) : GetAudioRecordsUseCase{
        return GetAudioRecordsUseCase(repository)
    }


    @Provides
    fun provideRenameAudioRecordUseCase(repository: LibraryRepository) : RenameAudioRecordUseCase {
        return RenameAudioRecordUseCase(repository)
    }


}
