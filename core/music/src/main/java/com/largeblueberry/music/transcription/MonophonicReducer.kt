package com.largeblueberry.music.transcription

import com.largeblueberry.music.basicpitch.BasicPitchConstants.MIDI_OFFSET
import com.largeblueberry.music.basicpitch.FrameNoteEvent
import com.largeblueberry.music.basicpitch.Posteriorgram

data class MonophonicParams(
    /** 겹치는 두 음에서 덜 두드러진 쪽의 이 비율 이상이 겹치면 버린다. */
    val dropOverlapRatio: Double = 0.5,
    /** 겹침을 잘라낸 뒤 이보다 짧으면 버린다(프레임, 86fps에서 약 70ms). */
    val minLengthFrames: Int = 6,
    /** 같은 음 두 조각 사이 간격이 이 이하일 때만 합칠지 검사한다(프레임). */
    val maxMergeGapFrames: Int = 3,
    /**
     * 경계 구간 note 확률의 최솟값이 (두 조각 진폭 중 작은 값 × 이 비율)보다 작으면 다시 낸 음으로 본다.
     * 합성 허밍에서 진짜 반복음은 0.46, 비브라토 조각은 0.97 이상이었다. 실제 허밍으로 다시 맞춰야 한다.
     */
    val rearticulationDipRatio: Double = 0.7,
    /** 경계 검사 창의 반폭(프레임). */
    val boundaryHalfWidthFrames: Int = 4,
)

/**
 * Basic Pitch(다성) 결과를 허밍용 단성 선율로 줄인다.
 * 1) 시간이 겹치는 음은 더 두드러진(진폭 × 길이) 쪽을 남기고, 나머지는 버리거나 겹치지 않게 자른다.
 * 2) 비브라토 때문에 같은 음이 잘게 쪼개진 조각을 합친다. 단, 경계에서 에너지가 꺼졌다 다시 켜진
 *    진짜 반복음은 합치지 않는다.
 */
object MonophonicReducer {

    fun reduce(
        events: List<FrameNoteEvent>,
        note: Posteriorgram,
        params: MonophonicParams = MonophonicParams(),
    ): List<FrameNoteEvent> {
        val accepted = resolveOverlaps(events, params)
        return mergeFragments(accepted, note, params)
    }

    private fun salience(e: FrameNoteEvent) = e.amplitude.toDouble() * (e.endFrame - e.startFrame)

    private fun resolveOverlaps(events: List<FrameNoteEvent>, params: MonophonicParams): List<FrameNoteEvent> {
        val ordered = events.sortedWith(
            compareByDescending<FrameNoteEvent> { salience(it) }.thenBy { it.startFrame }.thenBy { it.midi }
        )
        val accepted = ArrayList<FrameNoteEvent>()
        for (candidate in ordered) {
            val len = candidate.endFrame - candidate.startFrame
            if (len <= 0) continue
            var dropped = false
            // 받아들인 음들과 겹치지 않는 구간들만 남긴다
            var segments = listOf(candidate.startFrame to candidate.endFrame)
            for (a in accepted) {
                val overlap = minOf(candidate.endFrame, a.endFrame) - maxOf(candidate.startFrame, a.startFrame)
                if (overlap <= 0) continue
                if (overlap >= params.dropOverlapRatio * len) {
                    dropped = true
                    break
                }
                segments = segments.flatMap { (s, e) ->
                    buildList {
                        if (a.startFrame > s) add(s to minOf(e, a.startFrame))
                        if (a.endFrame < e) add(maxOf(s, a.endFrame) to e)
                    }
                }.filter { it.second > it.first }
            }
            if (dropped) continue
            val best = segments.maxByOrNull { it.second - it.first } ?: continue
            if (best.second - best.first < params.minLengthFrames) continue
            accepted.add(candidate.copy(startFrame = best.first, endFrame = best.second))
        }
        return accepted.sortedBy { it.startFrame }
    }

    private fun mergeFragments(
        notes: List<FrameNoteEvent>,
        note: Posteriorgram,
        params: MonophonicParams,
    ): List<FrameNoteEvent> {
        if (notes.isEmpty()) return notes
        val out = ArrayList<FrameNoteEvent>()
        var current = notes[0]
        for (next in notes.drop(1)) {
            if (shouldMerge(current, next, note, params)) {
                val l1 = current.endFrame - current.startFrame
                val l2 = next.endFrame - next.startFrame
                val amp = ((current.amplitude * l1 + next.amplitude * l2) / (l1 + l2).toFloat())
                current = current.copy(endFrame = maxOf(current.endFrame, next.endFrame), amplitude = amp)
            } else {
                out.add(current)
                current = next
            }
        }
        out.add(current)
        return out
    }

    private fun shouldMerge(a: FrameNoteEvent, b: FrameNoteEvent, note: Posteriorgram, params: MonophonicParams): Boolean {
        if (a.midi != b.midi) return false
        val gap = b.startFrame - a.endFrame
        if (gap > params.maxMergeGapFrames) return false
        val bin = a.midi - MIDI_OFFSET
        if (bin !in 0 until note.numBins) return false
        val lo = maxOf(0, minOf(a.endFrame, b.startFrame) - params.boundaryHalfWidthFrames)
        val hi = minOf(note.numFrames, maxOf(a.endFrame, b.startFrame) + params.boundaryHalfWidthFrames)
        if (hi <= lo) return true
        var minValue = Float.MAX_VALUE
        for (t in lo until hi) minValue = minOf(minValue, note[t, bin])
        val reference = minOf(a.amplitude, b.amplitude)
        return minValue >= params.rearticulationDipRatio * reference
    }
}
