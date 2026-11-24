package com.largeblueberry.library.ui.model

import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.model.TranscriptionFailure
import com.largeblueberry.domain.model.TranscriptionJob
import com.largeblueberry.domain.model.TranscriptionStage

/** 라이브러리 항목 하나의 채보 상태. 화면은 이 값만 보고 그린다. */
sealed interface RecordTranscription {
    /** 채보한 적 없음(또는 악보를 지움) */
    data object Idle : RecordTranscription
    data object Queued : RecordTranscription
    /** @param progress 0..1 */
    data class Running(val stage: TranscriptionStage, val progress: Float) : RecordTranscription
    /** 작업은 끝났는데 악보가 아직 목록에 안 보임(두 흐름의 도착 순서 차이) */
    data object Saving : RecordTranscription
    data class Failed(val reason: TranscriptionFailure) : RecordTranscription
    data class Done(val scoreId: String) : RecordTranscription
}

/** 녹음마다 가장 최근 악보의 id. 녹음과 연결이 끊긴 악보는 뺀다. */
internal fun latestScoreByRecord(scores: List<SheetMusic>): Map<Int, String> =
    scores.filter { it.recordId != null }
        .groupBy { it.recordId!! }
        .mapValues { (_, list) -> list.maxBy { it.createdAt }.id }

/**
 * 작업 상태(WorkManager)와 악보 목록(Room)을 합쳐 항목 상태를 정한다. 직전 상태를 받아 다음 상태를 낸다(scan).
 *
 * 두 흐름은 서로 다른 DB에서 와서 도착 순서가 보장되지 않는다(docs/troubleshooting.md B-18 결정 7).
 * - 악보가 있으면 무조건 [RecordTranscription.Done]. 악보가 먼저 와도, 작업 상태가 아직 진행 중이어도 같다.
 * - 직전에 대기·진행·저장 중이었는데 작업 상태가 사라졌고 악보가 아직 없으면 [RecordTranscription.Saving]을 유지한다.
 *   성공 신호는 DB 저장 뒤에만 나오므로 악보는 곧 나타난다. 앱을 새로 띄우면 직전 상태가 없어 여기에 갇히지 않는다.
 *
 * [RecordTranscription.Idle]은 맵에 넣지 않는다(없으면 Idle).
 */
internal fun reduceTranscriptions(
    previous: Map<Int, RecordTranscription>,
    jobs: Map<Int, TranscriptionJob>,
    scoreByRecord: Map<Int, String>,
): Map<Int, RecordTranscription> {
    val ids = previous.keys + jobs.keys + scoreByRecord.keys
    return ids.mapNotNull { id ->
        val score = scoreByRecord[id]
        val next = when {
            score != null -> RecordTranscription.Done(score)
            else -> when (val job = jobs[id]) {
                TranscriptionJob.Queued -> RecordTranscription.Queued
                is TranscriptionJob.Running -> RecordTranscription.Running(job.stage, job.progress)
                is TranscriptionJob.Failed -> RecordTranscription.Failed(job.reason)
                null -> if (previous[id].isInFlight()) RecordTranscription.Saving else null
            }
        }
        next?.let { id to it }
    }.toMap()
}

private fun RecordTranscription?.isInFlight() =
    this is RecordTranscription.Queued || this is RecordTranscription.Running || this is RecordTranscription.Saving
