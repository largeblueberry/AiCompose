package com.largeblueberry.data.repository

import com.largeblueberry.data.audio.AudioRecordDao
import com.largeblueberry.data.audio.AudioRecordEntity
import com.largeblueberry.data.score.ScoreDao
import com.largeblueberry.data.score.ScoreEntity
import com.largeblueberry.ml.BasicPitchModel
import com.largeblueberry.music.basicpitch.BasicPitchOutput
import com.largeblueberry.music.basicpitch.Posteriorgram
import com.largeblueberry.music.score.Key
import com.largeblueberry.music.score.Score
import com.largeblueberry.music.score.ScoreNote
import com.largeblueberry.music.transcription.ScoreTranscriber
import com.largeblueberry.music.transcription.TranscriptionResult
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 테스트가 실행 순서를 정하는 가짜 모델.
 * 호출마다 [Call]을 [calls]에 넣고, 테스트가 [Call.release]할 때까지 추론 안에 머문다.
 * 머무는 동안 진행률 콜백을 계속 부른다(실제 추론 루프처럼). 그래서 취소 확인이 콜백 안에서 일어난다.
 */
class GatedModel : BasicPitchModel {
    class Call(val samples: Int) {
        private val gate = CountDownLatch(1)
        fun release() = gate.countDown()
        internal fun awaitRelease(): Boolean = gate.await(POLL_MS, TimeUnit.MILLISECONDS)
    }

    private val calls = LinkedBlockingQueue<Call>()
    private val inside = AtomicInteger(0)
    val maxInside = AtomicInteger(0)

    /** true면 문 없이 바로 끝난다 */
    @Volatile var open = false
    @Volatile var failure: Exception? = null

    override fun predict(audio: FloatArray, onProgress: (done: Int, total: Int) -> Unit): BasicPitchOutput {
        val now = inside.incrementAndGet()
        maxInside.accumulateAndGet(now) { a, b -> maxOf(a, b) }
        try {
            failure?.let { throw it }
            if (!open) {
                val call = Call(audio.size)
                calls.put(call)
                val deadline = System.currentTimeMillis() + MAX_HOLD_MS
                while (!call.awaitRelease()) {
                    // 취소 확인이 빠지는 회귀가 생기면 여기서 영원히 돈다. 테스트가 멈추지 않고 실패하도록 상한을 둔다.
                    // Exception이 아니라 Error라 저장소가 실패 결과로 삼키지 못한다.
                    if (System.currentTimeMillis() > deadline) {
                        throw AssertionError("문이 ${MAX_HOLD_MS}ms 안에 열리지도, 취소로 빠져나오지도 않았다")
                    }
                    onProgress(0, 1)
                }
            }
            onProgress(1, 1)
            return BasicPitchOutput(Posteriorgram.empty(), Posteriorgram.empty())
        } finally {
            inside.decrementAndGet()
        }
    }

    /** 다음 모델 호출. [timeoutMs] 안에 없으면 null. */
    fun nextCall(timeoutMs: Long = 3_000): Call? = calls.poll(timeoutMs, TimeUnit.MILLISECONDS)

    private companion object {
        const val POLL_MS = 5L
        /** 테스트 전체 타임아웃(10초)보다 짧아야 한다 */
        const val MAX_HOLD_MS = 5_000L
    }
}

/** 모델 출력과 상관없이 정해 둔 악보를 돌려준다. */
class FixedTranscriber(
    @Volatile var notes: List<ScoreNote> = listOf(ScoreNote(60, 0, 4, velocity = 100)),
) : ScoreTranscriber {
    val calls = AtomicInteger(0)

    override fun transcribe(output: BasicPitchOutput, bpm: Int): TranscriptionResult {
        calls.incrementAndGet()
        return TranscriptionResult(Score(bpm, Key.C_MAJOR, notes), emptyList(), 0.0)
    }
}

class FakeAudioRecordDao : AudioRecordDao {
    private val records = ConcurrentHashMap<Int, AudioRecordEntity>()

    fun put(record: AudioRecordEntity) { records[record.id] = record }
    fun remove(id: Int) { records.remove(id) }

    override suspend fun getRecordById(id: Int): AudioRecordEntity? = records[id]
    override fun getAllRecords(): Flow<List<AudioRecordEntity>> = throw UnsupportedOperationException()
    override suspend fun insertRecord(record: AudioRecordEntity) = put(record)
    override suspend fun deleteRecord(record: AudioRecordEntity) = remove(record.id)
    override suspend fun renameRecord(record: AudioRecordEntity) = put(record)
}

/** 실제 DB처럼 외래 키를 검사하고(없는 녹음을 가리키면 실패) 같은 id는 덮어쓴다. */
class FakeScoreDao(private val records: FakeAudioRecordDao) : ScoreDao {
    val rows = ConcurrentHashMap<String, ScoreEntity>()

    override suspend fun getScoreById(id: String): ScoreEntity? = rows[id]
    override fun getAllScoresFlow(): Flow<List<ScoreEntity>> = throw UnsupportedOperationException()

    override suspend fun insertScore(score: ScoreEntity) {
        val parent = score.recordId
        if (parent != null && records.getRecordById(parent) == null) {
            throw IllegalStateException("FOREIGN KEY constraint failed")
        }
        rows[score.id] = score
    }

    override suspend fun deleteScore(score: ScoreEntity) { rows.remove(score.id) }
}
