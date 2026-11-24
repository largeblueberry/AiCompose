package com.largeblueberry.music.transcription

import com.largeblueberry.music.basicpitch.BasicPitchOutput
import com.largeblueberry.music.basicpitch.NoteCreation
import com.largeblueberry.music.basicpitch.NoteCreationParams
import com.largeblueberry.music.basicpitch.NoteEvent
import com.largeblueberry.music.score.Key
import com.largeblueberry.music.score.Score
import com.largeblueberry.music.score.ScoreNote
import kotlin.math.roundToInt

/** 허밍 채보 튜닝값. 실제 허밍으로 다시 맞출 값들이 여기 모인다. */
data class TranscriptionParams(
    val noteCreation: NoteCreationParams = NoteCreationParams.HUMMING,
    val monophonic: MonophonicParams = MonophonicParams(),
    val quantize: QuantizeParams = QuantizeParams(),
    /** null이면 자동 추정 */
    val key: Key? = null,
)

/**
 * 허밍용 [ScoreTranscriber]. Basic Pitch 출력 뒤의 모든 단계를 묶는다.
 * note creation(원본 포팅) → 단성화 → 위상 추정·양자화 → 조성 추정.
 *
 * 튜닝값은 생성할 때 한 번 받는다(호출마다 넘기지 않는다). 상태가 없어 여러 스레드에서 같이 써도 된다.
 */
class HummingTranscriber(
    private val params: TranscriptionParams = TranscriptionParams(),
) : ScoreTranscriber {

    override fun transcribe(output: BasicPitchOutput, bpm: Int): TranscriptionResult {
        val frameEvents = NoteCreation.toFrameEvents(output, params.noteCreation)
        val mono = MonophonicReducer.reduce(frameEvents, output.note, params.monophonic)
        val times = NoteCreation.modelFramesToTime(output.numFrames)
        val melody = mono.map { NoteEvent(times[it.startFrame], times[it.endFrame], it.midi, it.amplitude) }

        val q = Quantizer.quantize(melody, bpm, params.quantize)
        val key = params.key ?: KeyEstimator.estimate(q.notes.map { it.midi to it.lengthSteps.toDouble() })
        val notes = q.notes.map {
            ScoreNote(it.midi, it.startStep, it.lengthSteps, velocity = (it.amplitude * 127f).roundToInt().coerceIn(1, 127))
        }
        val score = Score(
            bpm = bpm,
            key = key,
            notes = notes,
            beatsPerBar = params.quantize.beatsPerBar,
            stepsPerBeat = params.quantize.stepsPerBeat,
        )
        return TranscriptionResult(score, melody, q.phaseSec)
    }
}
