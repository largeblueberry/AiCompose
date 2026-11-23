package com.largeblueberry.domain.usecase.sheetmusic

import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.repository.SheetMusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetScoresUseCase @Inject constructor(
    private val repository: SheetMusicRepository
) {
    operator fun invoke(): Flow<List<SheetMusic>> = repository.getAllScores()
}
