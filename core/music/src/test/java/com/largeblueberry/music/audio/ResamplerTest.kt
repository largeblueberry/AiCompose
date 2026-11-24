package com.largeblueberry.music.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

class ResamplerTest {

    private fun sine(freq: Double, rate: Int, seconds: Double) =
        FloatArray((rate * seconds).toInt()) { (0.5 * sin(2 * PI * freq * it / rate)).toFloat() }

    /** 가장자리 효과를 빼고 가운데 구간의 RMS */
    private fun rms(x: FloatArray): Double {
        val a = x.size / 10
        val b = x.size - a
        var s = 0.0
        for (i in a until b) s += x[i] * x[i].toDouble()
        return sqrt(s / (b - a))
    }

    @Test
    fun passesVoiceBand() {
        for (f in listOf(100.0, 440.0, 1000.0, 4000.0)) {
            val x = sine(f, 44_100, 0.5)
            val y = Resampler.decimateBy2(x)
            val gainDb = 20 * log10(rms(y) / rms(x))
            assertTrue("$f Hz gain $gainDb dB", gainDb > -0.5 && gainDb < 0.5)
        }
    }

    @Test
    fun rejectsAliasingBand() {
        // 22.05k로 줄이면 11.025k 위는 접혀 들어온다. 그 위 대역이 충분히 줄어야 한다.
        for (f in listOf(13_000.0, 16_000.0, 20_000.0)) {
            val x = sine(f, 44_100, 0.5)
            val y = Resampler.decimateBy2(x)
            val gainDb = 20 * log10(rms(y) / rms(x))
            assertTrue("$f Hz gain $gainDb dB", gainDb < -40)
        }
    }

    @Test
    fun lengthAndRates() {
        assertEquals(3, Resampler.decimateBy2(FloatArray(5)).size)
        val a = PcmAudio(22_050, FloatArray(10))
        assertSame(a, Resampler.toRate(a, 22_050))
        assertEquals(22_050, Resampler.toRate(PcmAudio(44_100, FloatArray(10)), 22_050).sampleRate)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnsupportedRate() {
        Resampler.toRate(PcmAudio(48_000, FloatArray(10)), 22_050)
    }
}
