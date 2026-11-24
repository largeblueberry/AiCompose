package com.largeblueberry.data.transcription

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.largeblueberry.domain.model.TranscriptionJob
import com.largeblueberry.domain.repository.TranscriptionScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * WorkManager로 채보를 돌린다(docs/troubleshooting.md B-18).
 *
 * - 녹음마다 독립된 작업([ExistingWorkPolicy.KEEP]). 하나의 APPEND 체인은 앞 작업이 실패하면 뒤가 모두 실패한다.
 *   모델을 한 번에 하나만 쓰는 건 저장소의 Mutex가 맡는다.
 * - 일반 작업(expedited 아님): API 31 미만의 expedited는 알림이 필요한데, 알림 없이 가기로 했다.
 * - 제약 조건 없음: 사용자가 직접 누른 작업이라 바로 돈다.
 */
class WorkManagerTranscriptionScheduler @Inject constructor(
    private val workManager: WorkManager,
) : TranscriptionScheduler {

    override fun enqueue(recordId: Int) {
        val request = OneTimeWorkRequestBuilder<TranscriptionWorker>()
            .setInputData(workDataOf(TranscriptionWorkContract.KEY_RECORD_ID to recordId))
            .addTag(TranscriptionWorkContract.TAG)
            .addTag(TranscriptionWorkContract.recordTag(recordId))
            .build()
        workManager.enqueueUniqueWork(TranscriptionWorkContract.uniqueName(recordId), ExistingWorkPolicy.KEEP, request)
    }

    override fun cancel(recordId: Int) {
        workManager.cancelUniqueWork(TranscriptionWorkContract.uniqueName(recordId))
    }

    override fun observeJobs(): Flow<Map<Int, TranscriptionJob>> =
        workManager.getWorkInfosByTagFlow(TranscriptionWorkContract.TAG)
            .map(TranscriptionWorkContract::toJobs)
            .distinctUntilChanged()
}
