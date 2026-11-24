package com.largeblueberry.domain.usecase.sheetmusic

import com.largeblueberry.domain.model.TranscriptionJob
import com.largeblueberry.domain.repository.TranscriptionScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** 녹음별 진행 중이거나 실패한 채보. 성공은 여기 없다(악보 목록이 결정한다). */
class ObserveTranscriptionsUseCase @Inject constructor(
    private val scheduler: TranscriptionScheduler,
) {
    operator fun invoke(): Flow<Map<Int, TranscriptionJob>> = scheduler.observeJobs()
}
