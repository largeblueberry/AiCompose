package com.largeblueberry.data.score

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM sheet_music WHERE id = :id")
    suspend fun getScoreById(id: String): ScoreEntity?

    @Query("SELECT * FROM sheet_music ORDER BY created_at DESC")
    fun getAllScoresFlow(): Flow<List<ScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntity)

    @Delete
    suspend fun deleteScore(score: ScoreEntity)
}
