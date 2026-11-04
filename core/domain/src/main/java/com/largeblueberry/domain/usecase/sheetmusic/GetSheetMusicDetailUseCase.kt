package com.largeblueberry.domain.usecase.sheetmusic

import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.repository.SheetMusicRepository
import javax.inject.Inject

class GetSheetMusicDetailUseCase @Inject constructor(
    private val repository: SheetMusicRepository
) {
    suspend operator fun invoke(requestBody: Any): Result<SheetMusic> {
        return try {
            repository.generateSheetMusic(requestBody)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
