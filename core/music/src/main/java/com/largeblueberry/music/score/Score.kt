package com.largeblueberry.music.score

/**
 * 양자화된 악보. 시간 단위는 step(= 1/stepsPerBeat 박, 기본 16분음표).
 * 지금은 4/4만 쓴다(docs/map/notes.md). beatsPerBar를 열어 둔 것은 나중을 위해서다.
 */
data class Score(
    val bpm: Int,
    val key: Key,
    val notes: List<ScoreNote>,
    val beatsPerBar: Int = 4,
    val stepsPerBeat: Int = 4,
) {
    init {
        require(bpm > 0) { "bpm must be > 0" }
        require(beatsPerBar > 0 && stepsPerBeat > 0)
        notes.zipWithNext().forEach { (a, b) ->
            require(a.endStep <= b.startStep) { "notes overlap or are unsorted: $a, $b" }
        }
        notes.forEach { require(it.startStep >= 0 && it.lengthSteps > 0) { "bad note $it" } }
    }

    val stepsPerBar: Int get() = beatsPerBar * stepsPerBeat

    /** 마지막 음을 담는 마디 끝까지의 길이. 음이 없으면 한 마디. */
    val totalSteps: Int
        get() {
            val end = notes.lastOrNull()?.endStep ?: 0
            val bars = maxOf(1, (end + stepsPerBar - 1) / stepsPerBar)
            return bars * stepsPerBar
        }

    val barCount: Int get() = totalSteps / stepsPerBar

    fun stepSeconds(): Double = 60.0 / bpm / stepsPerBeat
}

data class ScoreNote(
    val midi: Int,
    val startStep: Int,
    val lengthSteps: Int,
    /** MIDI velocity 1..127 */
    val velocity: Int,
) {
    val endStep: Int get() = startStep + lengthSteps
}
