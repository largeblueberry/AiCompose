package com.largeblueberry.music.transcription

import com.largeblueberry.music.basicpitch.NoteEvent
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

data class QuantizeParams(
    val stepsPerBeat: Int = 4,
    val beatsPerBar: Int = 4,
    /**
     * 위상(첫 박의 녹음 내 시각) 탐색 범위(박 단위). 녹음은 카운트인 직후부터라 첫 박이 0초 근처에 있고,
     * 기기 지연 때문에 대개 뒤로 밀린다. 그래서 뒤쪽을 더 넓게 본다.
     */
    val phaseSearchMinBeats: Double = -0.3,
    val phaseSearchMaxBeats: Double = 0.7,
    val phaseSearchResolutionSec: Double = 0.002,
    /** 8분·4분 격자에 가까운 onset을 더 좋게 보는 가중치(16분 격자 오차에 더한다). 박 위치 모호성을 줄인다. */
    val eighthGridWeight: Double = 0.25,
    val beatGridWeight: Double = 0.1,
    /** 다음 음 사이 빈틈이 max(이 step 수, 음 길이 × legatoFillRatio) 이하면 앞 음을 늘려 메운다. */
    val legatoFillSteps: Int = 1,
    val legatoFillRatio: Double = 0.25,
)

/** 박 격자에 맞춘 음. 시간 단위는 step. */
data class QuantizedNote(val midi: Int, val startStep: Int, val lengthSteps: Int, val amplitude: Float) {
    val endStep: Int get() = startStep + lengthSteps
}

data class QuantizeResult(
    val notes: List<QuantizedNote>,
    /** 추정한 첫 박의 녹음 내 시각(초). 앞의 빈 마디를 지운 만큼은 반영하지 않은 값. */
    val phaseSec: Double,
    /** 앞의 빈 마디를 지우며 당긴 step 수(디버깅용). */
    val shiftedSteps: Int,
)

/**
 * 알려진 BPM으로 초 단위 단성 음표를 박 격자에 맞춘다.
 * 기기마다 다른 녹음·재생 지연은 측정하지 않고, onset들이 격자에 가장 잘 맞는 위상을 찾아 흡수한다
 * (docs/map/notes.md "템포·박자").
 */
object Quantizer {

    fun quantize(notes: List<NoteEvent>, bpm: Int, params: QuantizeParams = QuantizeParams()): QuantizeResult {
        require(bpm > 0)
        if (notes.isEmpty()) return QuantizeResult(emptyList(), 0.0, 0)
        val sorted = notes.sortedBy { it.startSec }
        val beat = 60.0 / bpm
        val step = beat / params.stepsPerBeat
        val phase = estimatePhase(sorted, beat, params)

        // 1) 시작·끝을 격자에 반올림
        val raw = sorted.map { n ->
            val s = ((n.startSec - phase) / step).roundToInt()
            val e = ((n.endSec - phase) / step).roundToInt()
            QuantizedNote(n.midi, s, maxOf(1, e - s), n.amplitude)
        }

        // 2) 같은 step에서 시작하는 음은 더 두드러진 하나만 남긴다
        val unique = raw.groupBy { it.startStep }.values.map { group ->
            group.maxWith(compareBy<QuantizedNote> { it.amplitude * it.lengthSteps }.thenBy { -it.midi })
        }.sortedBy { it.startStep }

        // 3) 겹침 자르기, 짧은 빈틈 메우기(허밍은 적힌 길이보다 짧게 소리 내는 경우가 많다)
        val fitted = unique.mapIndexed { i, n ->
            val next = unique.getOrNull(i + 1)
            val limit = next?.startStep ?: Int.MAX_VALUE
            var end = minOf(n.endStep, limit)
            val fill = maxOf(params.legatoFillSteps, (n.lengthSteps * params.legatoFillRatio).toInt())
            if (next != null && next.startStep - end in 1..fill) {
                end = next.startStep
            } else {
                // 쉼표 앞·마지막 음: 끝이 다음 박 경계 바로 앞이면 박 경계까지 늘린다
                val toBeat = Math.floorMod(-end, params.stepsPerBeat)
                if (toBeat in 1..fill && end + toBeat <= limit) end += toBeat
            }
            n.copy(lengthSteps = maxOf(1, end - n.startStep))
        }

        // 4) 첫 음이 0보다 앞(못갖춘마디)이면 마디 단위로 밀고, 앞쪽 빈 마디는 지운다
        val stepsPerBar = params.stepsPerBeat * params.beatsPerBar
        val first = fitted.first().startStep
        val shiftBars = floor(first.toDouble() / stepsPerBar).toInt()
        val shift = shiftBars * stepsPerBar
        val shifted = fitted.map { it.copy(startStep = it.startStep - shift) }
        return QuantizeResult(shifted, phase, shift)
    }

    /**
     * 위상 φ를 탐색 범위에서 훑어, (onset - φ)의 16분 격자 오차² + 8분·4분 격자 오차²(가중)의 진폭 가중 합이
     * 가장 작은 φ를 고른다. 같은 비용이면 0에 가까운 쪽.
     */
    internal fun estimatePhase(notes: List<NoteEvent>, beat: Double, params: QuantizeParams): Double {
        val step = beat / params.stepsPerBeat
        val eighth = beat / 2
        val lo = params.phaseSearchMinBeats * beat
        val hi = params.phaseSearchMaxBeats * beat
        var best = 0.0
        var bestCost = Double.POSITIVE_INFINITY
        var phi = lo
        while (phi < hi) {
            var cost = 0.0
            for (n in notes) {
                val t = n.startSec - phi
                val w = n.amplitude.toDouble()
                cost += w * (sq(gridError(t, step)) +
                    params.eighthGridWeight * sq(gridError(t, eighth)) +
                    params.beatGridWeight * sq(gridError(t, beat)))
            }
            if (cost < bestCost - 1e-12 || (abs(cost - bestCost) <= 1e-12 && abs(phi) < abs(best))) {
                bestCost = cost
                best = phi
            }
            phi += params.phaseSearchResolutionSec
        }
        return best
    }

    private fun gridError(t: Double, grid: Double): Double {
        val r = t / grid
        return (r - Math.rint(r)) * grid
    }

    private fun sq(x: Double) = x * x
}
