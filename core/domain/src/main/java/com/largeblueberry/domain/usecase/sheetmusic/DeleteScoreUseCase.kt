package com.largeblueberry.domain.usecase.sheetmusic

import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.repository.SheetMusicRepository
import javax.inject.Inject

class DeleteScoreUseCase @Inject constructor(
    private val repository: SheetMusicRepository
) {
    suspend operator fun invoke(score: SheetMusic): Result<Unit> = repository.deleteScore(score)
}
