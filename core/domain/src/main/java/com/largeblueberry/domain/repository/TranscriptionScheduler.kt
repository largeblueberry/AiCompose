package com.largeblueberry.domain.repository

import com.largeblueberry.domain.model.TranscriptionJob
import kotlinx.coroutines.flow.Flow

/**
 * 채보를 화면 수명과 상관없이 돌린다. 앱이 죽어도 남는다.
 * domain·feature는 어떤 백그라운드 기술로 도는지 모른다.
 */
interface TranscriptionScheduler {
    /** 이미 대기·진행 중인 녹음이면 아무것도 하지 않는다. */
    fun enqueue(recordId: Int)

    /** 대기·진행 중인 작업을 멈춘다(녹음을 지울 때 내부 정리용). */
    fun cancel(recordId: Int)

    /** recordId → 진행 중이거나 실패한 작업. 끝났거나 없는 녹음은 빠진다. */
    fun observeJobs(): Flow<Map<Int, TranscriptionJob>>
}
