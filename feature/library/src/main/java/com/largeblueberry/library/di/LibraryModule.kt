package com.largeblueberry.library.di

import com.largeblueberry.library.domainLayer.repository.LibraryRepository
import com.largeblueberry.library.dataLayer.repository.impl.LibraryRepositoryImpl
import com.largeblueberry.library.domainLayer.usecase.DeleteAudioRecordUseCase
import com.largeblueberry.library.domainLayer.usecase.GetAudioRecordsUseCase
import com.largeblueberry.library.domainLayer.usecase.RenameAudioRecordUseCase
import com.largeblueberry.library.domainLayer.usecase.UploadAudioRecordUseCase
import com.largeblueberry.local.audio.AudioRecordDao
import com.largeblueberry.network.repository.AudioUploadRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LibraryModule {

    @Provides
    @Singleton
    fun provideLibraryRepository(dao: AudioRecordDao): LibraryRepository {
        return LibraryRepositoryImpl(dao)
    }

    @Provides
    fun provideDeleteAudioRecordUseCase(repository: LibraryRepository): DeleteAudioRecordUseCase {
        return DeleteAudioRecordUseCase(repository)
    }

    @Provides
    fun provideGetAudioRecordsUseCase(repository: LibraryRepository): GetAudioRecordsUseCase {
        return GetAudioRecordsUseCase(repository)
    }

    @Provides
    fun provideRenameAudioRecordUseCase(repository: LibraryRepository): RenameAudioRecordUseCase {
        return RenameAudioRecordUseCase(repository)
    }

    @Provides
    fun provideUploadAudioRecordUseCase(repository: AudioUploadRepository): UploadAudioRecordUseCase {
        return UploadAudioRecordUseCase(repository)
    }
}