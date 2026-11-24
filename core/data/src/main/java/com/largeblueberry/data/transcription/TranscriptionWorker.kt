package com.largeblueberry.data.transcription

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.largeblueberry.domain.model.TranscriptionException
import com.largeblueberry.domain.model.TranscriptionFailure
import com.largeblueberry.domain.model.TranscriptionStage
import com.largeblueberry.domain.repository.TranscriptionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 녹음 하나를 채보한다. 화면 수명과 상관없이 돌고, 프로세스가 죽으면 WorkManager가 다시 실행한다.
 *
 * - 입력은 녹음 id 하나. 악보 id는 작업 id에서 만들어 다시 실행돼도 악보가 하나다.
 * - 실패는 이유를 output에 담아 [Result.failure]. 같은 녹음은 다시 돌려도 같으므로 자동 재시도는 없다.
 * - 취소(시스템이 멈춤)는 실패로 바꾸지 않고 그대로 던진다. 그래야 WorkManager가 다시 예약한다.
 */
@HiltWorker
class TranscriptionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: TranscriptionRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val recordId = inputData.getInt(TranscriptionWorkContract.KEY_RECORD_ID, NO_RECORD)
        if (recordId == NO_RECORD) {
            Log.e(TAG, "work=$id has no record id")
            return Result.failure(TranscriptionWorkContract.failureData(TranscriptionFailure.OTHER))
        }
        val scoreId = TranscriptionWorkContract.scoreIdFor(id)
        Log.i(TAG, "work=$id record=$recordId attempt=$runAttemptCount start")

        val progress = ProgressThrottle { stage, p ->
            setProgressAsync(TranscriptionWorkContract.progressData(stage, p))
        }
        return repository.transcribe(recordId, scoreId, progress::report).fold(
            onSuccess = { sheet ->
                Log.i(TAG, "work=$id record=$recordId done score=${sheet.id}")
                Result.success(workDataOf(TranscriptionWorkContract.KEY_SCORE_ID to sheet.id))
            },
            onFailure = { e ->
                val reason = (e as? TranscriptionException)?.reason ?: TranscriptionFailure.OTHER
                Log.w(TAG, "work=$id record=$recordId failed: $reason")
                Result.failure(TranscriptionWorkContract.failureData(reason))
            },
        )
    }

    private companion object {
        const val TAG = "TranscriptionWorker"
        const val NO_RECORD = -1
    }
}

/**
 * setProgress는 호출마다 WorkManager DB에 쓰므로, 단계가 바뀌거나 1% 이상 움직였을 때만 넘긴다.
 * 모델 콜백 스레드에서 불린다(blocking 루프 안이라 suspend인 setProgress 대신 setProgressAsync).
 */
internal class ProgressThrottle(
    private val sink: (TranscriptionStage, Float) -> Unit,
) {
    private var lastStage: TranscriptionStage? = null
    private var lastProgress = -1f

    @Synchronized
    fun report(stage: TranscriptionStage, progress: Float) {
        if (stage == lastStage && progress - lastProgress < STEP && progress < 1f) return
        lastStage = stage
        lastProgress = progress
        sink(stage, progress)
    }

    private companion object {
        const val STEP = 0.01f
    }
}
