package com.largeblueberry.domain.model

/** 채보 파이프라인의 단계. 진행 표시용. */
enum class TranscriptionStage {
    /** 다른 녹음이 모델을 쓰고 있어 기다리는 중 */
    WAITING,
    /** 녹음 파일 읽기·리샘플 */
    READING,
    /** 모델 추론과 악보 만들기 */
    ANALYZING,
    /** 파일 쓰기·DB 저장 */
    SAVING,
}

/** 채보가 실패한 이유. 화면이 이유별로 다른 안내를 보여 준다. */
enum class TranscriptionFailure {
    /** 녹음에서 음을 찾지 못함 */
    NO_NOTES,
    /** 채보하는 동안 녹음이 지워짐 */
    RECORD_GONE,
    OTHER,
}

class TranscriptionException(
    val reason: TranscriptionFailure,
    cause: Throwable? = null,
) : Exception("transcription failed: $reason", cause)

/**
 * 녹음 하나의 진행 중이거나 실패한 채보 작업. 성공은 여기 없다:
 * "그 녹음의 악보가 있다"가 성공이고, 그건 악보 저장소가 단일 진실 공급원이다(docs/troubleshooting.md B-18).
 */
sealed interface TranscriptionJob {
    data object Queued : TranscriptionJob
    /** @param progress 0..1 */
    data class Running(val stage: TranscriptionStage, val progress: Float) : TranscriptionJob
    data class Failed(val reason: TranscriptionFailure) : TranscriptionJob
}
