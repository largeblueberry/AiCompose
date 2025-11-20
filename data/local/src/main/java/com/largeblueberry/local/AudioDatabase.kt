package com.largeblueberry.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.largeblueberry.local.audio.AudioRecordDao
import com.largeblueberry.local.audio.AudioRecordEntity
import com.largeblueberry.local.score.ScoreDao
import com.largeblueberry.local.score.ScoreEntity

@Database(
    entities = [
        AudioRecordEntity::class,
        ScoreEntity::class  // 새로 추가!
    ],
    version = 3, // 버전 업! (ScoreEntity 추가로 인한 스키마 변경)
    exportSchema = false
)
abstract class AudioDatabase : RoomDatabase() {
    abstract fun audioRecordDao(): AudioRecordDao
    abstract fun scoreDao(): ScoreDao  // 새로 추가!
}
/**
 * 데이터베이스 이름 변경! -> 중간에 스키마가 변경됨. room이 스키마를 혼동해서 이름을 새로 만듦.
 * 마이그레이션 하지 않은 이유는 이전 데이터가 필요 없기 때문.
 */