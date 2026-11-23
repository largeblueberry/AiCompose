package com.largeblueberry.domain.repository

import com.largeblueberry.domain.model.SheetMusic
import kotlinx.coroutines.flow.Flow

interface SheetMusicRepository {
    fun getAllScores(): Flow<List<SheetMusic>>
    suspend fun getScore(id: String): SheetMusic?
    suspend fun readMusicXml(score: SheetMusic): Result<String>

    /** DB 행과 악보 파일(MusicXML·MIDI)을 함께 지운다. */
    suspend fun deleteScore(score: SheetMusic): Result<Unit>
}
