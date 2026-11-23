package com.largeblueberry.data.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.roundToInt

/** 메트로놈이 AudioTrack에 이어 쓰는 한 박 버퍼. 박 간격이 흔들리지 않으려면 길이가 정확해야 한다. */
class MetronomeClickTest {

    private fun samplesPerBeat(bpm: Int) = (Metronome.SAMPLE_RATE * 60.0 / bpm).roundToInt()

    @Test
    fun bufferIsExactlyOneBeatLong() {
        for (bpm in listOf(40, 100, 137, 200)) {
            val spb = samplesPerBeat(bpm)
            assertEquals("bpm $bpm", spb, Metronome.click(1_000.0, spb).size)
        }
    }

    @Test
    fun clickIsOnlyAtTheStartThenSilence() {
        val spb = samplesPerBeat(100)
        val buf = Metronome.click(1_000.0, spb)
        val clickLen = (Metronome.SAMPLE_RATE * 0.03).toInt()
        assertTrue(buf.take(clickLen).any { abs(it.toInt()) > 1_000 })
        assertTrue(buf.drop(clickLen).all { it.toInt() == 0 })
    }

    @Test
    fun accentIsHigherPitchedThanNormalBeat() {
        val spb = samplesPerBeat(100)
        fun zeroCrossings(b: ShortArray) = b.toList().zipWithNext().count { (a, c) -> (a < 0) != (c < 0) }
        assertTrue(zeroCrossings(Metronome.click(1_600.0, spb)) > zeroCrossings(Metronome.click(1_000.0, spb)))
    }

    @Test
    fun clickLeavesHeadroom() {
        // 이어폰으로 바로 듣는 소리라 최대 음량의 70% 아래로 둔다(설계값 60%)
        val peak = Metronome.click(1_600.0, samplesPerBeat(200)).maxOf { abs(it.toInt()) }
        assertTrue("peak $peak", peak in 1..(Short.MAX_VALUE * 0.7).toInt())
    }
}
