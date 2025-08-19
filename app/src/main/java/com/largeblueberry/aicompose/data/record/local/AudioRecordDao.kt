package com.largeblueberry.aicompose.data.record.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordDao {
    @Query("SELECT * FROM audio_records ORDER BY created_at DESC")
    fun getAllRecords(): Flow<List<AudioRecordEntity>>

    @Insert
    suspend fun insertRecord(record: AudioRecordEntity)

    @Delete
    suspend fun deleteRecord(record: AudioRecordEntity)

    @Update
    suspend fun renameRecord(record: AudioRecordEntity)
}