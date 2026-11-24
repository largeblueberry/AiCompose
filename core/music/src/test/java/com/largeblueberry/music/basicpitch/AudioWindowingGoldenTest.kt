package com.largeblueberry.music.basicpitch

import com.largeblueberry.music.Golden
import com.largeblueberry.music.audio.Wav
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioWindowingGoldenTest {

    @Test
    fun windowCountAndUnwrapMatchPython() {
        for (case in listOf("c_major_100bpm", "short_1s")) {
            val meta = Golden.meta(case)
            val audio = Wav.read(Golden.bytes("$case.wav"))
            assertEquals(BasicPitchConstants.SAMPLE_RATE, audio.sampleRate)
            assertEquals(meta["numSamples"]!!.jsonPrimitive.int, audio.samples.size)

            val windows = AudioWindowing.windows(audio.samples)
            val rawCount = meta["rawWindows"]!!.jsonPrimitive.int
            assertEquals("$case window count", rawCount, windows.size)
            windows.forEach { assertEquals(BasicPitchConstants.AUDIO_N_SAMPLES, it.size) }

            val raw = Golden.f32("$case.raw_note.f32")
            val perWindow = BasicPitchConstants.ANNOT_N_FRAMES * 88
            val outputs = (0 until rawCount).map { raw.copyOfRange(it * perWindow, (it + 1) * perWindow) }
            val unwrapped = AudioWindowing.unwrap(outputs, 88, audio.samples.size)
            assertEquals(meta["numFrames"]!!.jsonPrimitive.int, unwrapped.numFrames)
            assertArrayEquals(Golden.f32("$case.note.f32"), unwrapped.values, 0f)
        }
    }

    @Test
    fun firstWindowIsLeftPaddedByHalfOverlap() {
        val audio = FloatArray(1000) { 1f }
        val w = AudioWindowing.windows(audio).single()
        val pad = BasicPitchConstants.OVERLAP_LEN / 2
        assertEquals(0f, w[pad - 1])
        assertEquals(1f, w[pad])
        assertEquals(1f, w[pad + 999])
        assertEquals(0f, w[pad + 1000])
    }

    @Test
    fun emptyAudioGivesNoFrames() {
        val windows = AudioWindowing.windows(FloatArray(0))
        // 원본도 패딩(3840 샘플) 때문에 창 1개를 만든다
        assertEquals(1, windows.size)
        assertEquals(0, AudioWindowing.unwrap(listOf(FloatArray(172 * 88)), 88, 0).numFrames)
    }
}
