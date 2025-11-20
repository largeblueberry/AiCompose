package com.largeblueberry.feature_sheetmusic.domain

import com.largeblueberry.feature_sheetmusic.domain.repository.SheetMusicRepository
import javax.inject.Inject

class GenerateSheetMusicUseCase @Inject constructor(
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

/**
 * 여기는 악보 상세 정보를 가져오는 usecase입니다.
 */