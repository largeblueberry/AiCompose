package com.largeblueberry.domain.usecase.sheetmusic

import com.largeblueberry.domain.repository.TranscriptionScheduler
import javax.inject.Inject

/** 녹음을 악보로 만들도록 맡긴다. 화면을 떠나도 계속되고, 같은 녹음을 여러 번 맡겨도 작업은 하나다. */
class EnqueueTranscriptionUseCase @Inject constructor(
    private val scheduler: TranscriptionScheduler,
) {
    operator fun invoke(recordId: Int) = scheduler.enqueue(recordId)
}
