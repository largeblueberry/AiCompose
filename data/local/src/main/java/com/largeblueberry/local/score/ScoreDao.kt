package com.largeblueberry.local.score

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM sheet_music WHERE id = :id")
    suspend fun getScoreById(id: String): ScoreEntity?

    @Query("SELECT * FROM sheet_music ORDER BY createdAt DESC")
    suspend fun getAllScores(): List<ScoreEntity>

    @Query("SELECT * FROM sheet_music ORDER BY createdAt DESC")
    fun getAllScoresFlow(): Flow<List<ScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntity)

    @Delete
    suspend fun deleteScore(score: ScoreEntity)

    @Query("DELETE FROM sheet_music WHERE id = :id")
    suspend fun deleteScoreById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM sheet_music WHERE id = :id)")
    suspend fun isScoreExists(id: String): Boolean
}