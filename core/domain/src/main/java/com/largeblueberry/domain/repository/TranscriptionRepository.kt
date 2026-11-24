package com.largeblueberry.domain.repository

import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.model.TranscriptionStage

/**
 * 녹음 → 악보 파이프라인(기기 안에서 처리). 백그라운드 작업이 부른다.
 *
 * 계약
 * - main-safe: 무거운 일은 구현이 알아서 다른 스레드로 넘긴다.
 * - 멱등: 같은 [scoreId]로 다시 불러도(작업이 다시 실행돼도) 악보는 하나다.
 * - 실패는 [com.largeblueberry.domain.model.TranscriptionException]을 담은 [Result.failure].
 *   취소는 Result로 감싸지 않고 CancellationException을 그대로 던진다.
 * - 모델은 한 번에 하나의 채보만 쓴다. 기다리는 동안 [TranscriptionStage.WAITING]을 알린다.
 */
interface TranscriptionRepository {
    /** @param onProgress 단계와 전체 진행률(0..1). 아무 스레드에서나 불린다. */
    suspend fun transcribe(
        recordId: Int,
        scoreId: String,
        onProgress: (TranscriptionStage, Float) -> Unit = { _, _ -> },
    ): Result<SheetMusic>
}
