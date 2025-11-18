package com.largeblueberry.feature_sheetmusic.domain

import javax.inject.Inject

class GetSheetMusicDetailUseCase @Inject constructor(
    private val sheetMusicRepository: SheetMusicRepository
) {
    suspend operator fun invoke(sheetMusicId: String): Result<SheetMusicDetail> {
        return try {
            val sheetMusic = sheetMusicRepository.getSheetMusicDetail(sheetMusicId)
            if (sheetMusic != null) {
                Result.success(sheetMusic)
            } else {
                Result.failure(Exception("해당 악보를 찾을 수 없습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 여기는 악보 상세 정보를 가져오는 usecase입니다.
 */