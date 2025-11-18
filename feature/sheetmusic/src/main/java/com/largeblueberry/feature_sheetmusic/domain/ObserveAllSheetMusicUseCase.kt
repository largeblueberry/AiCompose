package com.largeblueberry.feature_sheetmusic.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveAllSheetMusicUseCase @Inject constructor(
    private val sheetMusicRepository: SheetMusicRepository
) {
    operator fun invoke(): Flow<Result<List<SheetMusicDetail>>> {
        return sheetMusicRepository.getAllSheetMusicFlow()
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
}