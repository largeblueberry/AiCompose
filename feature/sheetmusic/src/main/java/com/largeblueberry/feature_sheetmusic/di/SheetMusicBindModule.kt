package com.largeblueberry.feature_sheetmusic.di

import com.largeblueberry.feature_sheetmusic.data.SheetMusicRepositoryImpl
import com.largeblueberry.feature_sheetmusic.domain.repository.SheetMusicRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SheetMusicModule {

    @Binds
    abstract fun bindSheetMusicRepository(
        impl: SheetMusicRepositoryImpl
    ): SheetMusicRepository
}