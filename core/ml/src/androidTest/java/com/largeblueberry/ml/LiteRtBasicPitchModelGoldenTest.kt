package com.largeblueberry.ml

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.largeblueberry.music.audio.Wav
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

/**
 * 기기에서 LiteRT로 돌린 결과가 Python(tensorflow 2.15 tflite) 결과와 같은지.
 * 골든 데이터는 :core:music 테스트 리소스를 androidTest assets로 쓴다(build.gradle.kts).
 * 기기·에뮬레이터가 있어야 돈다: ./gradlew :core:ml:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class LiteRtBasicPitchModelGoldenTest {

    private val testAssets = InstrumentationRegistry.getInstrumentation().context.assets
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    private fun f32(name: String): FloatArray {
        val bytes = testAssets.open("golden/$name").use { it.readBytes() }
        val fb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
        return FloatArray(fb.remaining()).also { fb.get(it) }
    }

    @Test
    fun matchesPythonOutput() {
        LiteRtBasicPitchModel.fromAssets(targetContext).use { model ->
            for (case in listOf("c_major_100bpm", "short_1s", "humanized_d_major_90bpm")) {
                val audio = testAssets.open("golden/$case.wav").use { Wav.read(it) }
                val out = model.predict(audio.samples)
                val expectedNote = f32("$case.note.f32")
                val expectedOnset = f32("$case.onset.f32")
                assertEquals("$case note size", expectedNote.size, out.note.values.size)
                // CPU·XNNPACK 차이로 아주 작은 오차는 날 수 있다
                val maxNote = expectedNote.indices.maxOf { abs(expectedNote[it] - out.note.values[it]) }
                val maxOnset = expectedOnset.indices.maxOf { abs(expectedOnset[it] - out.onset.values[it]) }
                assertTrue("$case note max diff $maxNote", maxNote < 1e-3f)
                assertTrue("$case onset max diff $maxOnset", maxOnset < 1e-3f)
            }
        }
    }
}
