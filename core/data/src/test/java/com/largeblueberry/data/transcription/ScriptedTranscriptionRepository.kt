package com.largeblueberry.data.transcription

import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.model.TranscriptionException
import com.largeblueberry.domain.model.TranscriptionFailure
import com.largeblueberry.domain.model.TranscriptionStage
import com.largeblueberry.domain.repository.TranscriptionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 테스트가 결과를 정해 줄 때까지 멈춰 있는 채보 저장소.
 * 부르는 쪽(Worker·WorkManager)이 실제로 어떤 순서로 무엇을 하는지만 본다.
 */
class ScriptedTranscriptionRepository : TranscriptionRepository {
    class Call(
        val recordId: Int,
        val scoreId: String,
        val onProgress: (TranscriptionStage, Float) -> Unit,
    ) {
        val result = CompletableDeferred<Result<SheetMusic>>()
        @Volatile var cancelled = false

        fun succeed() = result.complete(Result.success(sheet(scoreId, recordId)))
        fun fail(reason: TranscriptionFailure) = result.complete(Result.failure(TranscriptionException(reason)))
    }

    val calls = CopyOnWriteArrayList<Call>()
    private val latest = ConcurrentHashMap<Int, Call>()

    fun latest(recordId: Int): Call? = latest[recordId]

    override suspend fun transcribe(
        recordId: Int,
        scoreId: String,
        onProgress: (TranscriptionStage, Float) -> Unit,
    ): Result<SheetMusic> {
        val call = Call(recordId, scoreId, onProgress)
        calls += call
        latest[recordId] = call
        try {
            return call.result.await()
        } catch (e: CancellationException) {
            call.cancelled = true
            throw e
        }
    }

    companion object {
        fun sheet(id: String, recordId: Int) = SheetMusic(
            id = id,
            title = "녹음 $recordId",
            recordId = recordId,
            createdAt = 0,
            bpm = 100,
            beatsPerBar = 4,
            keyName = "C major",
            noteCount = 1,
            musicXmlPath = "/scores/$id.musicxml",
            midiPath = "/scores/$id.mid",
        )
    }
}
