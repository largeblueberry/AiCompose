package com.largeblueberry.music.basicpitch

import com.largeblueberry.music.Golden
import kotlinx.serialization.json.double
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

/** Kotlin 포팅이 Python basic-pitch 0.4.0 note_creation 과 같은 음표를 같은 순서로 내는지. */
class NoteCreationGoldenTest {

    @Test
    fun frameEventsMatchPythonForAllCasesAndVariants() {
        for (case in Golden.CASES) {
            val output = Golden.output(case)
            for ((name, variant) in Golden.variants(case)) {
                val expected = Golden.events(variant)
                val actual = NoteCreation.toFrameEvents(output, Golden.params(variant))
                val label = "$case/$name"
                assertEquals("$label event count", expected.size, actual.size)
                expected.zip(actual).forEachIndexed { i, (e, a) ->
                    assertEquals("$label[$i] start", e["startFrame"]!!.jsonPrimitive.int, a.startFrame)
                    assertEquals("$label[$i] end", e["endFrame"]!!.jsonPrimitive.int, a.endFrame)
                    assertEquals("$label[$i] midi", e["midi"]!!.jsonPrimitive.int, a.midi)
                    assertEquals("$label[$i] amp", e["amplitude"]!!.jsonPrimitive.float, a.amplitude, 1e-5f)
                }
            }
        }
    }

    @Test
    fun secondsMatchPython() {
        for (case in Golden.CASES) {
            val output = Golden.output(case)
            val variant = Golden.variants(case).getValue("default")
            val actual = NoteCreation.toNotes(output, Golden.params(variant))
            Golden.events(variant).zip(actual).forEach { (e, a) ->
                assertEquals(e["startSec"]!!.jsonPrimitive.double, a.startSec, 1e-9)
                assertEquals(e["endSec"]!!.jsonPrimitive.double, a.endSec, 1e-9)
            }
        }
    }

    @Test
    fun frameTimesMatchPython() {
        for (case in Golden.CASES) {
            val expected = Golden.meta(case)["frameTimesSec"]!!.jsonArray.map { it.jsonPrimitive.double }
            val actual = NoteCreation.modelFramesToTime(expected.size)
            expected.forEachIndexed { i, t -> assertEquals("$case frame $i", t, actual[i], 1e-9) }
        }
    }

    @Test
    fun emptyOutputGivesNoNotes() {
        val empty = BasicPitchOutput(Posteriorgram.empty(), Posteriorgram.empty())
        assertEquals(emptyList<NoteEvent>(), NoteCreation.toNotes(empty))
    }

    @Test
    fun silentOutputDoesNotProduceNaNOrNotes() {
        // 원본은 차분 최대가 0이면 NaN이 퍼진다. 포팅은 음표 없이 끝나야 한다.
        val frames = 50
        val zero = Posteriorgram(frames, 88, FloatArray(frames * 88))
        assertEquals(0, NoteCreation.toFrameEvents(BasicPitchOutput(zero, zero)).size)
    }
}
