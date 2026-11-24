package com.largeblueberry.music

import com.largeblueberry.music.basicpitch.BasicPitchOutput
import com.largeblueberry.music.basicpitch.NoteCreationParams
import com.largeblueberry.music.basicpitch.Posteriorgram
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * tools/basic_pitch_golden/make_golden.py 가 만든 골든 데이터.
 * 데이터를 바꾸려면 스크립트를 다시 돌리고 src/test/resources/golden 으로 복사한다.
 */
object Golden {
    val CASES = listOf("c_major_100bpm", "g_major_80bpm_legato", "short_1s", "humanized_d_major_90bpm", "quiet_noise")

    fun bytes(name: String): ByteArray =
        requireNotNull(javaClass.getResourceAsStream("/golden/$name")) { "missing golden/$name" }
            .use { it.readBytes() }

    fun meta(case: String): JsonObject =
        Json.parseToJsonElement(String(bytes("$case.json"), Charsets.UTF_8)).jsonObject

    fun f32(name: String): FloatArray {
        val buf = ByteBuffer.wrap(bytes(name)).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
        return FloatArray(buf.remaining()).also { buf.get(it) }
    }

    fun output(case: String): BasicPitchOutput {
        val m = meta(case)
        val frames = m["numFrames"]!!.jsonPrimitive.int
        val bins = m["numPitches"]!!.jsonPrimitive.int
        return BasicPitchOutput(
            note = Posteriorgram(frames, bins, f32("$case.note.f32")),
            onset = Posteriorgram(frames, bins, f32("$case.onset.f32")),
        )
    }

    fun params(variant: JsonObject): NoteCreationParams {
        val p = variant["params"]!!.jsonObject
        fun optD(k: String) = p[k].let { if (it == null || it is JsonNull) null else it.jsonPrimitive.double }
        return NoteCreationParams(
            onsetThreshold = p["onset_thresh"]!!.jsonPrimitive.double,
            frameThreshold = p["frame_thresh"]!!.jsonPrimitive.double,
            minNoteLenFrames = p["min_note_len"]!!.jsonPrimitive.int,
            inferOnsets = p["infer_onsets"]!!.jsonPrimitive.boolean,
            minFreqHz = optD("min_freq"),
            maxFreqHz = optD("max_freq"),
            melodiaTrick = p["melodia_trick"]!!.jsonPrimitive.boolean,
        )
    }

    data class TruthNote(val midi: Int, val startBeat: Double, val beats: Double)

    data class Truth(val bpm: Int, val startOffsetSec: Double, val key: String?, val notes: List<TruthNote>)

    fun truth(case: String): Truth {
        val t = meta(case)["truth"]!!.jsonObject
        val key = t["key"].let { if (it == null || it is JsonNull) null else it.jsonPrimitive.content }
        return Truth(
            bpm = t["bpm"]!!.jsonPrimitive.int,
            startOffsetSec = t["startOffsetSec"]!!.jsonPrimitive.double,
            key = key,
            notes = (t["notes"] as JsonArray).map {
                val o = it.jsonObject
                TruthNote(o["midi"]!!.jsonPrimitive.int, o["startBeat"]!!.jsonPrimitive.double, o["beats"]!!.jsonPrimitive.double)
            },
        )
    }

    fun variants(case: String): Map<String, JsonObject> =
        meta(case)["variants"]!!.jsonObject.mapValues { it.value.jsonObject }

    fun events(variant: JsonObject): List<JsonObject> = variant["events"]!!.jsonArray.map { it.jsonObject }
}
