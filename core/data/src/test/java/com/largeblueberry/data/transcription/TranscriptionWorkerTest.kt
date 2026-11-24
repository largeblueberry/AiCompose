package com.largeblueberry.data.transcription

import android.content.Context
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.largeblueberry.domain.model.TranscriptionFailure
import com.largeblueberry.domain.model.TranscriptionStage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * 실제 Worker(검증 대상)에 결과를 정해 주는 저장소를 끼운다.
 * 입력·악보 id·결과·실패 이유를 WorkManager의 언어(Result, Data)로 바르게 바꾸는지, 취소를 실패로 바꾸지 않는지 본다.
 */
@RunWith(RobolectricTestRunner::class)
class TranscriptionWorkerTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val repository = ScriptedTranscriptionRepository()

    private fun worker(input: Data = workDataOf(TranscriptionWorkContract.KEY_RECORD_ID to 1)) =
        TestListenableWorkerBuilder<TranscriptionWorker>(context)
            .setInputData(input)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters) =
                    TranscriptionWorker(appContext, workerParameters, repository)
            })
            .build()

    /** Worker를 돌리고, 저장소가 불리면 [respond]로 결과를 정한다. */
    private fun run(worker: TranscriptionWorker, respond: (ScriptedTranscriptionRepository.Call) -> Unit) =
        runBlocking {
            withTimeout(5_000) {
                val result = async { worker.doWork() }
                while (repository.calls.isEmpty()) yield()
                respond(repository.calls.single())
                result.await()
            }
        }

    @Test
    fun `악보 id를 작업 id에서 만들어 넘기고, 성공하면 그 id를 output에 담는다`() {
        val w = worker()

        val result = run(w) { it.succeed() }

        val call = repository.calls.single()
        assertEquals(1, call.recordId)
        assertEquals(TranscriptionWorkContract.scoreIdFor(w.id), call.scoreId)
        assertEquals(
            ListenableWorker.Result.success(workDataOf(TranscriptionWorkContract.KEY_SCORE_ID to call.scoreId)),
            result,
        )
    }

    @Test
    fun `실패하면 이유를 output에 담아 failure`() {
        val result = run(worker()) { it.fail(TranscriptionFailure.NO_NOTES) }

        assertEquals(
            ListenableWorker.Result.failure(TranscriptionWorkContract.failureData(TranscriptionFailure.NO_NOTES)),
            result,
        )
    }

    @Test
    fun `녹음 id가 없으면 저장소를 부르지 않고 OTHER로 실패`() = runBlocking {
        val result = worker(Data.EMPTY).doWork()

        assertEquals(
            ListenableWorker.Result.failure(TranscriptionWorkContract.failureData(TranscriptionFailure.OTHER)),
            result,
        )
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun `저장소가 취소되면 실패로 바꾸지 않고 그대로 던진다(그래야 WorkManager가 다시 예약한다)`() {
        try {
            run(worker()) { it.result.completeExceptionally(CancellationException("stopped by system")) }
            fail("취소가 Result로 바뀌어 돌아왔다")
        } catch (_: CancellationException) {
            // 기대한 대로: doWork가 취소를 그대로 던졌다
        }
    }

    // ── 진행률 줄이기 ──

    @Test
    fun `진행률은 단계가 바뀌거나 1퍼센트 이상 움직일 때만, 끝(1)은 항상 넘긴다`() {
        val sent = mutableListOf<Pair<TranscriptionStage, Float>>()
        val throttle = ProgressThrottle { s, p -> sent += s to p }

        throttle.report(TranscriptionStage.READING, 0f)
        throttle.report(TranscriptionStage.ANALYZING, 0.05f)
        throttle.report(TranscriptionStage.ANALYZING, 0.052f) // 0.2% → 버림
        throttle.report(TranscriptionStage.ANALYZING, 0.061f)
        throttle.report(TranscriptionStage.SAVING, 0.95f)
        throttle.report(TranscriptionStage.SAVING, 1f)

        assertEquals(
            listOf(
                TranscriptionStage.READING to 0f,
                TranscriptionStage.ANALYZING to 0.05f,
                TranscriptionStage.ANALYZING to 0.061f,
                TranscriptionStage.SAVING to 0.95f,
                TranscriptionStage.SAVING to 1f,
            ),
            sent,
        )
    }
}
