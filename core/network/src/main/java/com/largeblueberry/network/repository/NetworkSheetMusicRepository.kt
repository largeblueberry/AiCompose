package com.largeblueberry.network.repository

import com.largeblueberry.network.model.response.SheetMusicDto

interface NetworkSheetMusicRepository {
    suspend fun generateSheetMusic(requestBody: Any): Result<SheetMusicDto>
}