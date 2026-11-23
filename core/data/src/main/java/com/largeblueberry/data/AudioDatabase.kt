package com.largeblueberry.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.largeblueberry.data.audio.AudioRecordDao
import com.largeblueberry.data.audio.AudioRecordEntity
import com.largeblueberry.data.score.ScoreDao
import com.largeblueberry.data.score.ScoreEntity

@Database(
    entities = [
        AudioRecordEntity::class,
        ScoreEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AudioDatabase : RoomDatabase() {
    abstract fun audioRecordDao(): AudioRecordDao
    abstract fun scoreDao(): ScoreDao
}
/**
 * 데이터베이스 이름 변경! -> 중간에 스키마가 변경됨. room이 스키마를 혼동해서 이름을 새로 만듦.
 * 마이그레이션 하지 않은 이유는 이전 데이터가 필요 없기 때문.
 * v7: 녹음을 3GP에서 WAV로 전환. 녹음에 bpm·박자·샘플레이트 컬럼 추가.
 *     기존 3GP 녹음 행은 버린다(docs/map/notes.md "녹음").
 */