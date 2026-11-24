package com.largeblueberry.library.ui.model

import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.model.TranscriptionFailure
import com.largeblueberry.domain.model.TranscriptionJob
import com.largeblueberry.domain.model.TranscriptionStage
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 작업 상태 + 악보 목록 → 항목 상태(scan 한 걸음). docs/troubleshooting.md B-18 결정 7의 "도착 순서" 규칙을 고정한다.
 * 흐름을 한 걸음씩 밀어 보는 [steps]로 실제 도착 순서를 그대로 적는다.
 */
class RecordTranscriptionTest {

    private val running = TranscriptionJob.Running(TranscriptionStage.ANALYZING, 0.95f)

    /** (작업 상태, 녹음 id → 악보 id) 이벤트를 차례로 넣고 녹음 1의 상태 변화를 돌려준다 */
    private fun steps(vararg events: Pair<Map<Int, TranscriptionJob>, Map<Int, String>>): List<RecordTranscription> {
        var state = emptyMap<Int, RecordTranscription>()
        return events.map { (jobs, scores) ->
            state = reduceTranscriptions(state, jobs, scores)
            state[1] ?: RecordTranscription.Idle
        }
    }

    private val none = emptyMap<Int, TranscriptionJob>()
    private val noScore = emptyMap<Int, String>()

    @Test
    fun `성공 신호가 먼저, 악보가 나중에 와도 Idle로 깜빡이지 않는다`() {
        val seen = steps(
            mapOf(1 to running) to noScore,
            none to noScore, // 작업은 끝났는데(SUCCEEDED는 표시 안 함) 악보 목록은 아직
            none to mapOf(1 to "s1"),
        )

        assertEquals(
            listOf(RecordTranscription.Running(TranscriptionStage.ANALYZING, 0.95f), RecordTranscription.Saving, RecordTranscription.Done("s1")),
            seen,
        )
    }

    @Test
    fun `악보가 먼저, 성공 신호가 나중에 와도 바로 Done`() {
        val seen = steps(
            mapOf(1 to running) to noScore,
            mapOf(1 to running) to mapOf(1 to "s1"), // 악보가 먼저 보임
            none to mapOf(1 to "s1"),
        )

        assertEquals(
            listOf(RecordTranscription.Running(TranscriptionStage.ANALYZING, 0.95f), RecordTranscription.Done("s1"), RecordTranscription.Done("s1")),
            seen,
        )
    }

    @Test
    fun `대기에서 바로 끝나 진행을 못 봤어도 저장 중으로 이어진다`() {
        val seen = steps(mapOf(1 to TranscriptionJob.Queued) to noScore, none to noScore)

        assertEquals(listOf(RecordTranscription.Queued, RecordTranscription.Saving), seen)
    }

    @Test
    fun `앱을 새로 띄우면 직전 상태가 없어 저장 중에 갇히지 않는다`() {
        assertEquals(listOf(RecordTranscription.Idle), steps(none to noScore))
    }

    @Test
    fun `악보를 지우면 다시 Idle`() {
        val seen = steps(none to mapOf(1 to "s1"), none to noScore)

        assertEquals(listOf(RecordTranscription.Done("s1"), RecordTranscription.Idle), seen)
    }

    @Test
    fun `실패는 이유와 함께 보이고, 다시 맡기면 대기로 간다`() {
        val failed = TranscriptionJob.Failed(TranscriptionFailure.NO_NOTES)
        val seen = steps(
            mapOf(1 to running) to noScore,
            mapOf(1 to failed) to noScore,
            mapOf(1 to TranscriptionJob.Queued) to noScore,
        )

        assertEquals(
            listOf(
                RecordTranscription.Running(TranscriptionStage.ANALYZING, 0.95f),
                RecordTranscription.Failed(TranscriptionFailure.NO_NOTES),
                RecordTranscription.Queued,
            ),
            seen,
        )
    }

    @Test
    fun `녹음마다 따로 정해진다`() {
        val state = reduceTranscriptions(
            previous = emptyMap(),
            jobs = mapOf(1 to TranscriptionJob.Queued, 2 to TranscriptionJob.Failed(TranscriptionFailure.OTHER)),
            scoreByRecord = mapOf(3 to "s3"),
        )

        assertEquals(
            mapOf(
                1 to RecordTranscription.Queued,
                2 to RecordTranscription.Failed(TranscriptionFailure.OTHER),
                3 to RecordTranscription.Done("s3"),
            ),
            state,
        )
    }

    @Test
    fun `녹음마다 가장 최근 악보를 고르고, 연결이 끊긴 악보는 뺀다`() {
        fun score(id: String, recordId: Int?, at: Long) = SheetMusic(
            id = id, title = id, recordId = recordId, createdAt = at, bpm = 100, beatsPerBar = 4,
            keyName = "C major", noteCount = 1, musicXmlPath = "", midiPath = "",
        )

        val latest = latestScoreByRecord(
            listOf(score("old", 1, 10), score("new", 1, 20), score("other", 2, 5), score("orphan", null, 30))
        )

        assertEquals(mapOf(1 to "new", 2 to "other"), latest)
    }
}
