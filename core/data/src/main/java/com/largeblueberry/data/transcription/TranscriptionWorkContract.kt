package com.largeblueberry.data.transcription

import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.workDataOf
import com.largeblueberry.domain.model.TranscriptionFailure
import com.largeblueberry.domain.model.TranscriptionJob
import com.largeblueberry.domain.model.TranscriptionStage
import java.util.UUID

/**
 * Worker와 Scheduler가 함께 쓰는 약속(이름·키·태그)과 WorkInfo → [TranscriptionJob] 변환.
 * 변환은 순수 함수라 표(docs/troubleshooting.md B-18)를 그대로 테스트로 옮긴다.
 */
internal object TranscriptionWorkContract {
    /** 모든 채보 작업에 붙는 태그. 목록을 볼 때 쓴다. */
    const val TAG = "transcription"
    private const val RECORD_TAG_PREFIX = "transcription-record:"

    const val KEY_RECORD_ID = "record_id"
    const val KEY_STAGE = "stage"
    const val KEY_PROGRESS = "progress"
    const val KEY_REASON = "reason"
    const val KEY_SCORE_ID = "score_id"

    /** 녹음마다 하나(같은 녹음을 여러 번 눌러도 작업은 하나). */
    fun uniqueName(recordId: Int) = "transcribe-$recordId"

    fun recordTag(recordId: Int) = "$RECORD_TAG_PREFIX$recordId"

    /** 악보 id를 작업 id에서 만든다. 작업이 다시 실행돼도 같은 악보를 덮어쓴다(B-18 결정 5). */
    fun scoreIdFor(workId: UUID) = "score_$workId"

    fun progressData(stage: TranscriptionStage, progress: Float): Data =
        workDataOf(KEY_STAGE to stage.name, KEY_PROGRESS to progress)

    fun failureData(reason: TranscriptionFailure): Data = workDataOf(KEY_REASON to reason.name)

    /**
     * 녹음별로 화면에 보일 작업. 같은 녹음에 작업 기록이 여럿이면(실패 뒤 다시 시도 등)
     * 진행 중인 것 > 성공(=표시하지 않음, 악보가 결정) > 실패 순으로 고른다.
     */
    fun toJobs(infos: List<WorkInfo>): Map<Int, TranscriptionJob> =
        infos.mapNotNull { info -> recordIdOf(info)?.let { it to info } }
            .groupBy({ it.first }, { it.second })
            .mapNotNull { (recordId, works) -> pick(works)?.let { recordId to it } }
            .toMap()

    private fun pick(works: List<WorkInfo>): TranscriptionJob? {
        works.firstOrNull { !it.state.isFinished }?.let { return toJob(it) }
        if (works.any { it.state == WorkInfo.State.SUCCEEDED }) return null
        return works.firstOrNull { it.state == WorkInfo.State.FAILED }?.let(::toJob)
    }

    fun toJob(info: WorkInfo): TranscriptionJob? = when (info.state) {
        WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> TranscriptionJob.Queued
        WorkInfo.State.RUNNING -> {
            val stage = info.progress.getString(KEY_STAGE)
                ?.let { runCatching { TranscriptionStage.valueOf(it) }.getOrNull() }
                ?: TranscriptionStage.READING
            if (stage == TranscriptionStage.WAITING) TranscriptionJob.Queued
            else TranscriptionJob.Running(stage, info.progress.getFloat(KEY_PROGRESS, 0f))
        }
        WorkInfo.State.FAILED -> TranscriptionJob.Failed(
            info.outputData.getString(KEY_REASON)
                ?.let { runCatching { TranscriptionFailure.valueOf(it) }.getOrNull() }
                ?: TranscriptionFailure.OTHER
        )
        // 성공은 악보 저장소가, 취소는 녹음 삭제가 결정한다
        WorkInfo.State.SUCCEEDED, WorkInfo.State.CANCELLED -> null
    }

    private fun recordIdOf(info: WorkInfo): Int? =
        info.tags.firstOrNull { it.startsWith(RECORD_TAG_PREFIX) }?.removePrefix(RECORD_TAG_PREFIX)?.toIntOrNull()
}
