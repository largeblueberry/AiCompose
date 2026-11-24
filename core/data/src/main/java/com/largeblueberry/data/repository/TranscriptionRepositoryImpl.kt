package com.largeblueberry.data.repository

import android.util.Log
import com.largeblueberry.data.audio.AudioRecordDao
import com.largeblueberry.data.di.ScoresDirectory
import com.largeblueberry.data.mapper.ScoreMapper
import com.largeblueberry.data.score.ScoreDao
import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.model.TranscriptionException
import com.largeblueberry.domain.model.TranscriptionFailure
import com.largeblueberry.domain.model.TranscriptionStage
import com.largeblueberry.domain.repository.TranscriptionRepository
import com.largeblueberry.ml.BasicPitchModel
import com.largeblueberry.music.audio.Resampler
import com.largeblueberry.music.audio.Wav
import com.largeblueberry.music.basicpitch.BasicPitchConstants
import com.largeblueberry.music.basicpitch.BasicPitchOutput
import com.largeblueberry.music.export.MidiWriter
import com.largeblueberry.music.export.MusicXmlWriter
import com.largeblueberry.music.transcription.ScoreTranscriber
import dagger.Lazy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 녹음(WAV) → Basic Pitch(:core:ml) → 악보(:core:music) → MusicXML·MIDI 파일 + DB.
 * 파이프라인은 분기 없이 한 줄로 흐르고, 단계 사이마다 취소를 확인한다(docs/troubleshooting.md B-18).
 *
 * 경쟁 상태 규칙(B-18 결정 7)
 * - 모델: [modelLock] 하나로 한 번에 하나만. 잠그는 곳은 [predict] 한 군데뿐이다(Mutex는 재진입이 안 된다).
 *   그래서 이 클래스는 앱에 하나여야 한다([Singleton]).
 * - 파일: 실행마다 다른 임시 이름으로 쓰고 최종 이름으로 원자적 이동. DB는 같은 [scoreId]로 덮어쓴다.
 *   같은 작업이 겹쳐 돌거나 다시 돌아도 악보는 하나다.
 * - 채보 중 녹음 삭제: 어느 단계에서 실패하든 녹음이 없어졌으면 [TranscriptionFailure.RECORD_GONE].
 */
@Singleton
class TranscriptionRepositoryImpl @Inject constructor(
    /** 처음 채보할 때 연다. 열기도 잠금 안에서 한다. */
    private val model: Lazy<BasicPitchModel>,
    private val transcriber: ScoreTranscriber,
    private val recordDao: AudioRecordDao,
    private val scoreDao: ScoreDao,
    @ScoresDirectory private val scoresDir: File,
) : TranscriptionRepository {

    private val modelLock = Mutex()

    override suspend fun transcribe(
        recordId: Int,
        scoreId: String,
        onProgress: (TranscriptionStage, Float) -> Unit,
    ): Result<SheetMusic> = withContext(Dispatchers.Default) {
        // runCatching은 CancellationException까지 삼키므로 쓰지 않는다
        try {
            Result.success(pipeline(recordId, scoreId, onProgress))
        } catch (e: CancellationException) {
            throw e
        } catch (e: TranscriptionException) {
            Log.w(TAG, "record=$recordId score=$scoreId failed: ${e.reason}")
            Result.failure(e)
        } catch (e: Exception) {
            val reason = if (recordDao.getRecordById(recordId) == null) TranscriptionFailure.RECORD_GONE
            else TranscriptionFailure.OTHER
            Log.e(TAG, "record=$recordId score=$scoreId failed: $reason", e)
            Result.failure(TranscriptionException(reason, e))
        }
    }

    private suspend fun pipeline(
        recordId: Int,
        scoreId: String,
        onProgress: (TranscriptionStage, Float) -> Unit,
    ): SheetMusic {
        val record = recordDao.getRecordById(recordId)
            ?: throw TranscriptionException(TranscriptionFailure.RECORD_GONE)

        onProgress(TranscriptionStage.READING, 0f)
        val wav = File(record.filePath).inputStream().buffered().use { Wav.read(it) }
        val audio = Resampler.toRate(wav, BasicPitchConstants.SAMPLE_RATE)
        removeLeftovers(scoreId)
        currentCoroutineContext().ensureActive()

        val output = predict(audio.samples, onProgress)
        val result = transcriber.transcribe(output, record.bpm)
        Log.i(TAG, "record=$recordId notes=${result.score.notes.size} key=${result.score.key.name} phase=${result.phaseSec}")
        if (result.score.notes.isEmpty()) throw TranscriptionException(TranscriptionFailure.NO_NOTES)
        currentCoroutineContext().ensureActive()

        onProgress(TranscriptionStage.SAVING, SAVING_START)
        val title = record.filename.substringBeforeLast('.')
        val xmlFile = File(scoresDir, "$scoreId.musicxml")
        val midiFile = File(scoresDir, "$scoreId.mid")
        writeAtomically(xmlFile, MusicXmlWriter.write(result.score, title = title).toByteArray(Charsets.UTF_8))
        writeAtomically(midiFile, MidiWriter.write(result.score))

        val sheet = SheetMusic(
            id = scoreId,
            title = title,
            recordId = record.id,
            createdAt = System.currentTimeMillis(),
            bpm = result.score.bpm,
            beatsPerBar = result.score.beatsPerBar,
            keyName = result.score.key.name,
            noteCount = result.score.notes.size,
            musicXmlPath = xmlFile.absolutePath,
            midiPath = midiFile.absolutePath,
        )
        try {
            // 같은 id는 덮어쓴다(REPLACE). 녹음이 그새 지워졌으면 외래 키 때문에 여기서 실패한다.
            scoreDao.insertScore(ScoreMapper.toEntity(sheet))
        } catch (e: Exception) {
            xmlFile.delete()
            midiFile.delete()
            throw e
        }
        onProgress(TranscriptionStage.SAVING, 1f)
        return sheet
    }

    /** 모델 구간. 잠금은 여기서만 잡는다. */
    private suspend fun predict(
        samples: FloatArray,
        onProgress: (TranscriptionStage, Float) -> Unit,
    ): BasicPitchOutput {
        // 표시용 힌트일 뿐이라 검사와 잠금 사이가 어긋나도 괜찮다(잠금 자체는 withLock이 지킨다)
        if (modelLock.isLocked) onProgress(TranscriptionStage.WAITING, ANALYZING_START)
        return modelLock.withLock {
            // 추론은 blocking 루프라 취소를 스스로 모른다. 진행률 콜백마다 확인해 예외로 빠져나와야 잠금이 풀린다.
            val job = currentCoroutineContext()[Job]
            onProgress(TranscriptionStage.ANALYZING, ANALYZING_START)
            model.get().predict(samples) { done, total ->
                job?.ensureActive()
                onProgress(TranscriptionStage.ANALYZING, ANALYZING_START + ANALYZING_SPAN * done / total)
            }
        }
    }

    /** 실행마다 다른 임시 이름으로 쓰고 최종 이름으로 옮긴다. 겹친 실행끼리 서로의 파일을 덮어쓰지 않는다. */
    private fun writeAtomically(target: File, bytes: ByteArray) {
        scoresDir.mkdirs()
        val tmp = File(scoresDir, "${target.name}.${UUID.randomUUID()}$TMP_SUFFIX")
        try {
            tmp.writeBytes(bytes)
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            tmp.delete()
        }
    }

    /** 이전 실행이 쓰다 죽으며 남긴 임시 파일. */
    private fun removeLeftovers(scoreId: String) {
        scoresDir.listFiles { f -> f.name.startsWith("$scoreId.") && f.name.endsWith(TMP_SUFFIX) }
            ?.forEach { it.delete() }
    }

    companion object {
        private const val TAG = "Transcription"
        /** FileProvider 공유 경로(app/src/main/res/xml/file_paths.xml)와 같아야 한다. */
        const val SCORES_DIR = "scores"
        private const val TMP_SUFFIX = ".tmp"
        private const val ANALYZING_START = 0.05f
        private const val ANALYZING_SPAN = 0.85f
        private const val SAVING_START = 0.95f
    }
}
