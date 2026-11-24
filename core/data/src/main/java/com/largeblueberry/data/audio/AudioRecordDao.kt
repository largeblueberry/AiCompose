package com.largeblueberry.data.audio

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

    @Query("SELECT * FROM audio_records WHERE id = :id")
    suspend fun getRecordById(id: Int): AudioRecordEntity?

    @Insert
    suspend fun insertRecord(record: AudioRecordEntity)

    @Delete
    suspend fun deleteRecord(record: AudioRecordEntity)

    @Update
    suspend fun renameRecord(record: AudioRecordEntity)
}