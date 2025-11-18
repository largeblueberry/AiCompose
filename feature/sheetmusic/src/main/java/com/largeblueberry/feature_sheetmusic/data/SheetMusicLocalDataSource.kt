package com.largeblueberry.feature_sheetmusic.data

import com.largeblueberry.feature_sheetmusic.domain.SheetMusicDetail
import kotlinx.coroutines.flow.Flow

interface SheetMusicLocalDataSource {
    suspend fun getSheetMusicDetail(id: String): SheetMusicDetail?
    suspend fun getAllSheetMusic(): List<SheetMusicDetail>
    fun getAllSheetMusicFlow(): Flow<List<SheetMusicDetail>>
    suspend fun saveSheetMusicDetail(sheetMusic: SheetMusicDetail)
    suspend fun deleteSheetMusicDetail(id: String)
    suspend fun isSheetMusicExists(id: String): Boolean
}