package com.largeblueberry.feature_sheetmusic.di

import com.largeblueberry.feature_sheetmusic.data.SheetMusicLocalDataSource
import com.largeblueberry.feature_sheetmusic.data.SheetMusicLocalDataSourceImpl
import com.largeblueberry.feature_sheetmusic.data.SheetMusicRepositoryImpl
import com.largeblueberry.feature_sheetmusic.domain.SheetMusicRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SheetMusicBindModule {

    @Binds
    @Singleton
    abstract fun bindSheetMusicRepository(
        sheetMusicRepositoryImpl: SheetMusicRepositoryImpl
    ): SheetMusicRepository

    @Binds
    abstract fun bindSheetMusicLocalDataSource(
        sheetMusicLocalDataSourceImpl: SheetMusicLocalDataSourceImpl
    ): SheetMusicLocalDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object SheetMusicProvideModule {

    // UseCase들이 있다면 여기에 추가
    // @Provides
    // fun provideGetSheetMusicUseCase(repository: SheetMusicRepository): GetSheetMusicUseCase {
    //     return GetSheetMusicUseCase(repository)
    // }
}