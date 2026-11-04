package com.largeblueberry.data.di

import com.largeblueberry.data.repository.LanguageRepositoryImpl
import com.largeblueberry.data.repository.LibraryRepositoryImpl
import com.largeblueberry.data.repository.RecordRepositoryImpl
import com.largeblueberry.data.repository.SheetMusicRepositoryImpl
import com.largeblueberry.data.repository.ThemeRepositoryImpl
import com.largeblueberry.domain.repository.LanguageRepository
import com.largeblueberry.domain.repository.LibraryRepository
import com.largeblueberry.domain.repository.RecordRepository
import com.largeblueberry.domain.repository.SheetMusicRepository
import com.largeblueberry.domain.repository.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        impl: ThemeRepositoryImpl
    ): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindLanguageRepository(
        impl: LanguageRepositoryImpl
    ): LanguageRepository

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(
        impl: LibraryRepositoryImpl
    ): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindRecordRepository(
        impl: RecordRepositoryImpl
    ): RecordRepository

    @Binds
    @Singleton
    abstract fun bindSheetMusicRepository(
        impl: SheetMusicRepositoryImpl
    ): SheetMusicRepository
}
