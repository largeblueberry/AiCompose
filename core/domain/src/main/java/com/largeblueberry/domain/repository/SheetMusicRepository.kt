package com.largeblueberry.domain.repository

import com.largeblueberry.domain.model.SheetMusic
import kotlinx.coroutines.flow.Flow

interface SheetMusicRepository {
    suspend fun generateSheetMusic(requestBody: Any): Result<SheetMusic>
    suspend fun saveSheetMusic(sheetMusic: SheetMusic): Result<Unit>
    fun getAllScores(): Flow<List<SheetMusic>>
    suspend fun deleteScore(score: SheetMusic): Result<Unit>
}
