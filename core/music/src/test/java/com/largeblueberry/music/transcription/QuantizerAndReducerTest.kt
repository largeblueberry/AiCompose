package com.largeblueberry.music.transcription

import com.largeblueberry.music.basicpitch.FrameNoteEvent
import com.largeblueberry.music.basicpitch.NoteEvent
import com.largeblueberry.music.basicpitch.Posteriorgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

class QuantizerAndReducerTest {

    /** 정답 (midi, 시작 박, 길이 박) → 지연·흔들림·짧게 소리 낸 초 단위 음표 */
    private fun perform(
        notes: List<Triple<Int, Double, Double>>,
        bpm: Int,
        latency: Double,
        jitter: Double,
        seed: Int,
        soundRatio: Double = 0.85,
    ): List<NoteEvent> {
        val r = Random(seed)
        val beat = 60.0 / bpm
        return notes.map { (m, s, l) ->
            val j = (r.nextDouble() * 2 - 1) * jitter
            val start = latency + s * beat + j
            NoteEvent(start, start + l * beat * soundRatio, m, 0.8f)
        }
    }

    private val melody = listOf(
        Triple(60, 0.0, 1.0), Triple(62, 1.0, 0.5), Triple(64, 1.5, 0.5), Triple(65, 2.0, 2.0),
        Triple(67, 4.0, 1.0), Triple(69, 5.0, 1.0), Triple(67, 6.0, 0.75), Triple(65, 6.75, 0.25),
        Triple(64, 7.0, 1.0), Triple(62, 8.0, 1.0), Triple(60, 9.0, 3.0),
    )

    private fun expectedSteps(notes: List<Triple<Int, Double, Double>>) =
        notes.map { Triple(it.first, (it.second * 4).toInt(), (it.third * 4).toInt()) }

    @Test
    fun recoversMelodyAcrossLatenciesAndTempos() {
        for (bpm in listOf(60, 90, 120, 140)) {
            val beat = 60.0 / bpm
            // 지연은 탐색 범위(-0.3 ~ 0.7박) 안에서 고른다
            for (latencyBeats in listOf(-0.2, 0.0, 0.15, 0.4, 0.6)) {
                for (seed in 1..3) {
                    // 흔들림: 16분음표 길이의 ±20%
                    val perf = perform(melody, bpm, latencyBeats * beat, jitter = 0.2 * beat / 4, seed = seed)
                    val q = Quantizer.quantize(perf, bpm)
                    val actual = q.notes.map { Triple(it.midi, it.startStep, it.lengthSteps) }
                    assertEquals("bpm=$bpm latency=${latencyBeats}beat seed=$seed", expectedSteps(melody), actual)
                    assertTrue(abs(q.phaseSec - latencyBeats * beat) < beat / 8)
                }
            }
        }
    }

    @Test
    fun pickupBeforeFirstBarGetsItsOwnBar() {
        // 카운트인 직후 박보다 먼저(못갖춘마디처럼) 부른 경우: 첫 박 앞 8분음표 하나
        val notes = listOf(Triple(67, -0.5, 0.5), Triple(72, 0.0, 1.0), Triple(71, 1.0, 1.0))
        val bpm = 100
        val beat = 0.6
        val perf = notes.map { (m, s, l) -> NoteEvent(0.1 + s * beat, 0.1 + (s + l * 0.9) * beat, m, 0.8f) }
        val q = Quantizer.quantize(perf, bpm)
        assertEquals(listOf(Triple(67, 14, 2), Triple(72, 16, 4), Triple(71, 20, 4)),
            q.notes.map { Triple(it.midi, it.startStep, it.lengthSteps) })
    }

    @Test
    fun leadingEmptyBarsAreRemoved() {
        val bpm = 120
        val perf = listOf(NoteEvent(4.0 + 0.05, 4.4, 60, 0.8f), NoteEvent(4.5 + 0.05, 4.95, 62, 0.8f)) // 3번째 마디에서 시작
        val q = Quantizer.quantize(perf, bpm)
        assertEquals(0, q.notes.first().startStep)
        assertEquals(32, q.shiftedSteps)
    }

    @Test
    fun collidingNotesKeepTheStrongerOne() {
        val perf = listOf(NoteEvent(0.0, 0.5, 60, 0.9f), NoteEvent(0.03, 0.12, 62, 0.3f), NoteEvent(0.5, 1.0, 64, 0.9f))
        val q = Quantizer.quantize(perf, 120)
        assertEquals(listOf(60, 64), q.notes.map { it.midi })
    }

    @Test
    fun emptyInput() {
        assertEquals(0, Quantizer.quantize(emptyList(), 100).notes.size)
    }

    private fun flat(frames: Int, value: Float = 0.9f) = Posteriorgram(frames, 88, FloatArray(frames * 88) { value })

    @Test
    fun reducerDropsMostlyOverlappedWeakerNote() {
        // 같은 구간의 옥타브 위 배음(약함) → 버린다
        val events = listOf(FrameNoteEvent(10, 60, 60, 0.8f), FrameNoteEvent(12, 58, 72, 0.4f))
        val out = MonophonicReducer.reduce(events, flat(100))
        assertEquals(listOf(60), out.map { it.midi })
    }

    @Test
    fun reducerTrimsSmallLegatoOverlap() {
        val events = listOf(FrameNoteEvent(10, 52, 60, 0.8f), FrameNoteEvent(50, 90, 62, 0.8f))
        val out = MonophonicReducer.reduce(events, flat(100))
        assertEquals(2, out.size)
        assertTrue(out[0].endFrame <= out[1].startFrame)
    }

    @Test
    fun reducerMergesVibratoFragmentsButKeepsRearticulation() {
        val frames = 120
        val values = FloatArray(frames * 88) { 0.9f }
        val bin = 67 - 21
        // 60프레임 근처에서 에너지가 꺼졌다 켜짐 = 다시 낸 음
        for (t in 58..61) values[t * 88 + bin] = 0.2f
        val note = Posteriorgram(frames, 88, values)
        val events = listOf(
            FrameNoteEvent(10, 30, 67, 0.9f), FrameNoteEvent(30, 59, 67, 0.9f), // 비브라토 조각 → 합침
            FrameNoteEvent(59, 100, 67, 0.9f), // 경계에 골 → 따로
        )
        val out = MonophonicReducer.reduce(events, note)
        assertEquals(listOf(10 to 59, 59 to 100), out.map { it.startFrame to it.endFrame })
    }
}
