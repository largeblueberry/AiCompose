package com.largeblueberry.data.transcription

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.largeblueberry.domain.model.TranscriptionFailure
import com.largeblueberry.domain.model.TranscriptionJob
import com.largeblueberry.domain.model.TranscriptionStage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * 실제 WorkManager(테스트 초기화) 위에서 Scheduler를 돌린다. 저장소만 결과를 정해 주는 가짜다.
 * 작업 등록 정책(KEEP), 취소, 진행률·결과가 observeJobs까지 오는 길을 본다.
 */
@RunWith(RobolectricTestRunner::class)
class WorkManagerTranscriptionSchedulerTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val repository = ScriptedTranscriptionRepository()
    private lateinit var workManager: WorkManager
    private lateinit var scheduler: WorkManagerTranscriptionScheduler

    @Before
    fun setUp() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters) =
                    TranscriptionWorker(appContext, workerParameters, repository)
            })
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
        scheduler = WorkManagerTranscriptionScheduler(workManager)
    }

    private fun test(block: suspend () -> Unit) = runBlocking { withTimeout(10_000) { block() } }

    private suspend fun awaitJobs(predicate: (Map<Int, TranscriptionJob>) -> Boolean) =
        scheduler.observeJobs().first(predicate)

    private suspend fun awaitCall(recordId: Int, count: Int): ScriptedTranscriptionRepository.Call {
        while (repository.calls.count { it.recordId == recordId } < count) kotlinx.coroutines.delay(5)
        return repository.latest(recordId)!!
    }

    @Test
    fun `등록하면 실행되고, 진행률이 observeJobs까지 온다`() = test {
        scheduler.enqueue(1)
        val call = awaitCall(1, 1)

        call.onProgress(TranscriptionStage.ANALYZING, 0.5f)

        awaitJobs { it[1] == TranscriptionJob.Running(TranscriptionStage.ANALYZING, 0.5f) }
        call.succeed()
    }

    @Test
    fun `진행 중인 녹음을 다시 등록해도 작업은 하나다(KEEP)`() = test {
        scheduler.enqueue(1)
        val call = awaitCall(1, 1)

        scheduler.enqueue(1)
        scheduler.enqueue(1)

        val works = workManager.getWorkInfosForUniqueWork(TranscriptionWorkContract.uniqueName(1)).get()
        assertEquals(1, works.size)
        assertEquals(1, repository.calls.size)
        call.succeed()
    }

    @Test
    fun `성공하면 observeJobs에서 빠진다(악보 저장소가 결정)`() = test {
        scheduler.enqueue(1)
        awaitCall(1, 1).succeed()

        awaitJobs { 1 !in it }
    }

    @Test
    fun `실패하면 이유가 보이고, 다시 등록하면 새 작업(새 악보 id)으로 돈다`() = test {
        scheduler.enqueue(1)
        val first = awaitCall(1, 1)
        first.fail(TranscriptionFailure.NO_NOTES)
        awaitJobs { it[1] == TranscriptionJob.Failed(TranscriptionFailure.NO_NOTES) }

        scheduler.enqueue(1)
        val second = awaitCall(1, 2)

        assertNotEquals(first.scoreId, second.scoreId)
        awaitJobs { it[1] !is TranscriptionJob.Failed }
        second.succeed()
    }

    @Test
    fun `녹음마다 따로 돈다(하나가 실패해도 다른 녹음은 영향이 없다)`() = test {
        scheduler.enqueue(1)
        scheduler.enqueue(2)
        val one = awaitCall(1, 1)
        val two = awaitCall(2, 1)

        one.fail(TranscriptionFailure.NO_NOTES)
        two.succeed()

        awaitJobs { it == mapOf(1 to TranscriptionJob.Failed(TranscriptionFailure.NO_NOTES)) }
    }

    @Test
    fun `취소하면 저장소의 채보가 취소되고 observeJobs에서 빠진다`() = test {
        scheduler.enqueue(1)
        val call = awaitCall(1, 1)

        scheduler.cancel(1)

        awaitJobs { 1 !in it }
        while (!call.cancelled) kotlinx.coroutines.delay(5)
        assertTrue(call.cancelled)
    }
}
