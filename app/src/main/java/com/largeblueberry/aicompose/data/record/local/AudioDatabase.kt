package com.largeblueberry.aicompose.data.record.local

import androidx.room.*


@Database(
    entities = [AudioRecordEntity::class],
    version = 1, // 새 데이터베이스니까 버전 1
    exportSchema = false
)
// ✅ @TypeConverters 줄 완전히 삭제!
abstract class AudioDatabase : RoomDatabase() {
    abstract fun audioRecordDao(): AudioRecordDao

}
/**
 * 데이터베이스 이름 변경! -> 중간에 스키마가 변경됨. room이 스키마를 혼동해서 이름을 새로 만듦.
 * 마이그레이션 하지 않은 이유는 이전 데이터가 필요 없기 때문.
 */