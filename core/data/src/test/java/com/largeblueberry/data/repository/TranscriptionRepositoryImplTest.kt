package com.largeblueberry.data.repository

import com.largeblueberry.data.audio.AudioRecordEntity
import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.model.TranscriptionException
import com.largeblueberry.domain.model.TranscriptionFailure
import com.largeblueberry.domain.model.TranscriptionStage
import com.largeblueberry.music.audio.PcmAudio
import com.largeblueberry.music.audio.Wav
import com.largeblueberry.music.basicpitch.BasicPitchConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Collections

/**
 * 채보 파이프라인 저장소. 검증 대상(저장소)은 실제 객체, 모델·채보기·DAO는 가짜(docs/troubleshooting.md B-17).
 * WAV 읽기·MusicXML/MIDI 만들기·파일 쓰기는 실제로 한다.
 *
 * 저장소가 실제 Dispatchers.Default에서 돌므로 가상 시간(runTest) 대신 runBlocking과 실제 시간 타임아웃을 쓴다(B-14).
 * 경쟁은 반복 실행이 아니라 [GatedModel]로 실행 순서를 직접 만들어 확인한다(B-18 결정 7).
 */
class TranscriptionRepositoryImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val model = GatedModel()
    private val transcriber = FixedTranscriber()
    private val recordDao = FakeAudioRecordDao()
    private val scoreDao = FakeScoreDao(recordDao)
    private val scoresDir by lazy { File(tmp.root, "scores") }
    private val repository by lazy {
        TranscriptionRepositoryImpl({ model }, transcriber, recordDao, scoreDao, scoresDir)
    }

    /** 녹음마다 길이를 달리해 모델 호출이 어느 녹음 것인지 알 수 있게 한다(샘플 수 = id × 100). */
    private fun record(id: Int) {
        val wav = File(tmp.root, "rec$id.wav")
        wav.writeBytes(Wav.encode(PcmAudio(BasicPitchConstants.SAMPLE_RATE, FloatArray(id * 100))))
        recordDao.put(
            AudioRecordEntity(
                id = id,
                filename = "녹음 $id.wav",
                filePath = wav.absolutePath,
                fileSize = wav.length(),
                duration = "0:01",
                bpm = 100,
                beatsPerBar = 4,
                sampleRate = BasicPitchConstants.SAMPLE_RATE,
            )
        )
    }

    private val stages = Collections.synchronizedList(mutableListOf<Pair<Int, TranscriptionStage>>())

    /**
     * 저장소가 실제로 값을 돌려준 녹음. 취소된 코루틴은 본문이 값을 돌려줘도 isCancelled가 true라,
     * "취소를 Result로 감싸지 않는다"는 계약은 이것으로만 확인할 수 있다.
     */
    private val returned = Collections.synchronizedMap(mutableMapOf<Int, Result<SheetMusic>>())

    private fun CoroutineScope.start(recordId: Int, scoreId: String = "score_$recordId"): Deferred<Result<SheetMusic>> =
        async(Dispatchers.Default) {
            repository.transcribe(recordId, scoreId) { s, _ -> stages += recordId to s }
                .also { returned[recordId] = it }
        }

    private fun failureOf(result: Result<SheetMusic>): TranscriptionFailure =
        (result.exceptionOrNull() as TranscriptionException).reason

    private fun files() = scoresDir.listFiles()?.map { it.name }?.sorted().orEmpty()

    private fun waitUntil(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 3_000
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "condition not met in time" }
            Thread.sleep(5)
        }
    }

    private fun test(block: suspend CoroutineScope.() -> Unit) = runBlocking { withTimeout(10_000) { block() } }

    // ── 파이프라인 ──

    @Test
    fun `성공하면 정해진 id로 파일 둘과 DB 행 하나를 남기고 진행률은 줄지 않는다`() = test {
        record(1)
        model.open = true
        val progress = mutableListOf<Float>()

        val sheet = repository.transcribe(1, "score_w1") { _, p -> progress += p }.getOrThrow()

        assertEquals("score_w1", sheet.id)
        assertEquals(1, sheet.recordId)
        assertEquals("녹음 1", sheet.title)
        assertEquals(listOf("score_w1.mid", "score_w1.musicxml"), files())
        assertTrue(File(sheet.musicXmlPath).readText().contains("<score-partwise"))
        assertEquals(setOf("score_w1"), scoreDao.rows.keys)
        assertEquals(progress.sorted(), progress)
        assertEquals(1f, progress.last())
    }

    @Test
    fun `같은 id로 다시 실행해도 악보는 하나고, 전에 죽으며 남긴 임시 파일은 지운다`() = test {
        record(1)
        model.open = true
        scoresDir.mkdirs()
        File(scoresDir, "score_w1.musicxml.dead-run.tmp").writeText("half")

        repository.transcribe(1, "score_w1").getOrThrow()
        repository.transcribe(1, "score_w1").getOrThrow()

        assertEquals(setOf("score_w1"), scoreDao.rows.keys)
        assertEquals(listOf("score_w1.mid", "score_w1.musicxml"), files())
    }

    @Test
    fun `시작할 때 녹음이 없으면 RECORD_GONE이고 모델을 부르지 않는다`() = test {
        val result = repository.transcribe(99, "score_w1")

        assertEquals(TranscriptionFailure.RECORD_GONE, failureOf(result))
        assertNull(model.nextCall(timeoutMs = 0))
        assertEquals(0, transcriber.calls.get())
    }

    @Test
    fun `음을 못 찾으면 NO_NOTES이고 아무것도 남기지 않는다`() = test {
        record(1)
        model.open = true
        transcriber.notes = emptyList()

        val result = repository.transcribe(1, "score_w1")

        assertEquals(TranscriptionFailure.NO_NOTES, failureOf(result))
        assertEquals(emptyList<String>(), files())
        assertTrue(scoreDao.rows.isEmpty())
    }

    @Test
    fun `녹음이 남아 있는데 실패하면 OTHER`() = test {
        record(1)
        model.failure = IllegalStateException("interpreter")

        assertEquals(TranscriptionFailure.OTHER, failureOf(repository.transcribe(1, "score_w1")))
    }

    // ── 경쟁 상태(B-18 결정 7) ──

    @Test
    fun `채보 중 녹음이 지워지면 저장의 외래 키 실패를 RECORD_GONE으로 바꾸고 파일을 지운다`() = test {
        record(1)
        val a = start(1, "score_w1")
        val call = model.nextCall()!!

        recordDao.remove(1)
        call.release()

        assertEquals(TranscriptionFailure.RECORD_GONE, failureOf(a.await()))
        assertTrue(scoreDao.rows.isEmpty())
        assertEquals(emptyList<String>(), files())
    }

    @Test
    fun `동시에 둘을 시작해도 모델 안에는 하나만 있고, 뒤의 것은 WAITING을 알린 뒤 차례를 기다린다`() = test {
        record(1)
        record(2)
        val a = start(1)
        val first = model.nextCall()!!
        val b = start(2)

        assertNull("앞의 것이 끝나기 전에 두 번째가 모델에 들어왔다", model.nextCall(timeoutMs = 300))
        assertTrue(2 to TranscriptionStage.WAITING in stages)

        first.release()
        model.nextCall()!!.release()

        assertTrue(a.await().isSuccess)
        assertTrue(b.await().isSuccess)
        assertEquals(1, model.maxInside.get())
    }

    @Test
    fun `잠금을 기다리다 취소되면 대기열에서 빠지고 다음 것이 들어간다`() = test {
        record(1)
        record(2)
        record(3)
        val a = start(1)
        val first = model.nextCall()!!
        val b = start(2)
        waitUntil { 2 to TranscriptionStage.WAITING in stages }

        b.cancel()
        first.release()
        val c = start(3)
        val next = model.nextCall()!!

        assertEquals("취소된 녹음 2가 모델에 들어왔다", 300, next.samples)
        next.release()
        assertTrue(a.await().isSuccess)
        assertTrue(c.await().isSuccess)
        assertTrue(b.isCancelled)
    }

    @Test
    fun `추론 중에 취소되면 Result로 감싸지 않고 취소되며, 잠금이 풀려 다음 것이 들어간다`() = test {
        record(1)
        record(2)
        val a = start(1)
        model.nextCall()!! // 1의 문은 열어 주지 않는다
        val b = start(2)

        a.cancel() // 진행률 콜백의 취소 확인으로 추론에서 빠져나와야 잠금이 풀린다
        val next = model.nextCall()

        assertNotNull("취소된 작업이 잠금을 쥐고 있다", next)
        assertEquals(200, next!!.samples)
        next.release()
        assertTrue(b.await().isSuccess)
        assertTrue(a.isCancelled)
        assertFalse("취소가 실패 결과로 감싸여 돌아왔다", returned.containsKey(1))
        assertFalse(scoreDao.rows.containsKey("score_1"))
    }
}
