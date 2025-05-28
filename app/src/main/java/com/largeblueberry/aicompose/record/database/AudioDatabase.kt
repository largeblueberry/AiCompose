// AudioDatabase.kt
package com.largeblueberry.aicompose.record.database

import android.content.Context
import androidx.room.*

@Database(
    entities = [AudioRecordEntity::class],
    version = 1, // 새 데이터베이스니까 버전 1
    exportSchema = false
)
// ✅ @TypeConverters 줄 완전히 삭제!
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
                    "audio_database_v2"


                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
/**
 * 데이터베이스 이름 변경! -> 중간에 스키마가 변경됨. room이 스키마를 혼동해서 이름을 새로 만듦.
 * 마이그레이션 하지 않은 이유는 이전 데이터가 필요 없기 때문.
 */
