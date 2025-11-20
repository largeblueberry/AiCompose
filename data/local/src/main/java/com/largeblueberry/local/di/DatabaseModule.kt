package com.largeblueberry.local.di

import android.content.Context
import androidx.room.Room
import com.largeblueberry.local.AudioDatabase
import com.largeblueberry.local.audio.AudioRecordDao
import com.largeblueberry.local.score.ScoreDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAudioDatabase(@ApplicationContext context: Context): AudioDatabase {
        return Room.databaseBuilder(
            context,
            AudioDatabase::class.java,
            "audio_database_v2"
        )
            .fallbackToDestructiveMigration() // 개발 중이므로 추가
            .build()
    }

    @Provides
    fun provideAudioRecordDao(database: AudioDatabase): AudioRecordDao {
        return database.audioRecordDao()
    }

    @Provides
    fun provideScoreDao(database: AudioDatabase): ScoreDao {
        return database.scoreDao()
    }
}