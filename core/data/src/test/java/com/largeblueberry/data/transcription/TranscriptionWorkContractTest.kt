package com.largeblueberry.data.transcription

import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State
import com.largeblueberry.domain.model.TranscriptionFailure
import com.largeblueberry.domain.model.TranscriptionJob
import com.largeblueberry.domain.model.TranscriptionStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

/** WorkInfo → TranscriptionJob 변환. docs/troubleshooting.md B-18의 표를 그대로 옮겼다. */
class TranscriptionWorkContractTest {

    private fun info(
        recordId: Int?,
        state: State,
        progress: Data = Data.EMPTY,
        output: Data = Data.EMPTY,
    ) = WorkInfo(
        id = UUID.randomUUID(),
        state = state,
        tags = setOfNotNull(TranscriptionWorkContract.TAG, recordId?.let(TranscriptionWorkContract::recordTag)),
        outputData = output,
        progress = progress,
    )

    private fun job(state: State, progress: Data = Data.EMPTY, output: Data = Data.EMPTY) =
        TranscriptionWorkContract.toJob(info(1, state, progress, output))

    // ── 표: 작업 하나 ──

    @Test
    fun `ENQUEUED와 BLOCKED는 대기`() {
        assertEquals(TranscriptionJob.Queued, job(State.ENQUEUED))
        assertEquals(TranscriptionJob.Queued, job(State.BLOCKED))
    }

    @Test
    fun `RUNNING인데 모델을 기다리는 중이면 대기`() {
        val progress = TranscriptionWorkContract.progressData(TranscriptionStage.WAITING, 0.05f)
        assertEquals(TranscriptionJob.Queued, job(State.RUNNING, progress))
    }

    @Test
    fun `RUNNING은 단계와 진행률`() {
        val progress = TranscriptionWorkContract.progressData(TranscriptionStage.ANALYZING, 0.5f)
        assertEquals(TranscriptionJob.Running(TranscriptionStage.ANALYZING, 0.5f), job(State.RUNNING, progress))
    }

    @Test
    fun `막 시작해 진행 정보가 아직 없으면 읽기 0`() {
        assertEquals(TranscriptionJob.Running(TranscriptionStage.READING, 0f), job(State.RUNNING))
    }

    @Test
    fun `FAILED는 output의 이유, 없거나 모르는 값이면 OTHER`() {
        val noNotes = TranscriptionWorkContract.failureData(TranscriptionFailure.NO_NOTES)
        assertEquals(TranscriptionJob.Failed(TranscriptionFailure.NO_NOTES), job(State.FAILED, output = noNotes))
        assertEquals(TranscriptionJob.Failed(TranscriptionFailure.OTHER), job(State.FAILED))
        val unknown = Data.Builder().putString(TranscriptionWorkContract.KEY_REASON, "FUTURE_REASON").build()
        assertEquals(TranscriptionJob.Failed(TranscriptionFailure.OTHER), job(State.FAILED, output = unknown))
    }

    @Test
    fun `SUCCEEDED와 CANCELLED는 표시하지 않는다(악보 유무·녹음 삭제가 결정)`() {
        assertNull(job(State.SUCCEEDED))
        assertNull(job(State.CANCELLED))
    }

    // ── 녹음별로 고르기 ──

    @Test
    fun `녹음별로 묶고, 녹음 태그가 없는 작업은 버린다`() {
        val jobs = TranscriptionWorkContract.toJobs(
            listOf(info(1, State.ENQUEUED), info(2, State.FAILED), info(null, State.ENQUEUED), info(3, State.SUCCEEDED))
        )
        assertEquals(
            mapOf(1 to TranscriptionJob.Queued, 2 to TranscriptionJob.Failed(TranscriptionFailure.OTHER)),
            jobs,
        )
    }

    @Test
    fun `실패 뒤 다시 시도 중이면 진행 중인 것을 보인다`() {
        val jobs = TranscriptionWorkContract.toJobs(listOf(info(1, State.FAILED), info(1, State.ENQUEUED)))
        assertEquals(mapOf(1 to TranscriptionJob.Queued), jobs)
    }

    @Test
    fun `실패 뒤 다시 시도해 성공했으면 예전 실패를 보이지 않는다`() {
        val jobs = TranscriptionWorkContract.toJobs(listOf(info(1, State.FAILED), info(1, State.SUCCEEDED)))
        assertEquals(emptyMap<Int, TranscriptionJob>(), jobs)
    }
}
