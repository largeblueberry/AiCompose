package com.largeblueberry.music.transcription

import com.largeblueberry.music.basicpitch.BasicPitchOutput
import com.largeblueberry.music.basicpitch.NoteEvent
import com.largeblueberry.music.score.Score

/**
 * 모델 출력 → 악보. 다른 계층(:core:data)이 쓰는 진입점이라 인터페이스로 둔다.
 * 구현을 바꾸거나(다른 채보 전략·튜닝값) 테스트에서 가짜로 바꿔 끼울 수 있게 한다.
 * 안쪽 단계(NoteCreation, MonophonicReducer, Quantizer, KeyEstimator)는 상태 없는 순수 함수라 object로 둔다.
 */
interface ScoreTranscriber {
    fun transcribe(output: BasicPitchOutput, bpm: Int): TranscriptionResult
}

class TranscriptionResult(
    val score: Score,
    /** 단성화 후 초 단위 음표(양자화 전). 디버깅·재양자화용. */
    val melody: List<NoteEvent>,
    /** 추정한 첫 박의 녹음 내 시각(초) */
    val phaseSec: Double,
)
