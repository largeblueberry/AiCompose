package com.largeblueberry.aicompose.record.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AudioRecordEntity::class], version = 1, exportSchema = false)
abstract class AudioDatabase : RoomDatabase() {
    abstract fun audioRecordDao(): AudioRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AudioDatabase? = null

        fun getDatabase(context: Context): AudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AudioDatabase::class.java,
                    "audio_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}