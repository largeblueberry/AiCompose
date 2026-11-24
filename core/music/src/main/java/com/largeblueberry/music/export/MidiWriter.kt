package com.largeblueberry.music.export

import com.largeblueberry.music.score.Score
import java.io.ByteArrayOutputStream

/**
 * Score → 표준 MIDI 파일(format 0, 트랙 하나). 악보와 같은 박 격자를 쓴다.
 * 템포·박자표·조표 메타 이벤트를 넣어 DAW·악보 앱에서 그대로 열리게 한다.
 */
object MidiWriter {

    const val TICKS_PER_QUARTER = 480

    fun write(score: Score, program: Int = 0, channel: Int = 0): ByteArray {
        require(program in 0..127 && channel in 0..15)
        val ticksPerStep = TICKS_PER_QUARTER / score.stepsPerBeat
        require(ticksPerStep * score.stepsPerBeat == TICKS_PER_QUARTER) { "stepsPerBeat must divide 480" }

        data class Ev(val tick: Int, val order: Int, val bytes: ByteArray)
        val events = ArrayList<Ev>()
        val usPerQuarter = 60_000_000 / score.bpm
        events.add(Ev(0, 0, byteArrayOf(0xFF.toByte(), 0x51, 0x03,
            (usPerQuarter shr 16).toByte(), (usPerQuarter shr 8).toByte(), usPerQuarter.toByte())))
        // 박자표: 분자, 분모(2의 거듭제곱 지수), 메트로놈 클록, 32분음표 수
        events.add(Ev(0, 0, byteArrayOf(0xFF.toByte(), 0x58, 0x04, score.beatsPerBar.toByte(), 2, 24, 8)))
        events.add(Ev(0, 0, byteArrayOf(0xFF.toByte(), 0x59, 0x02, score.key.fifths.toByte(), if (score.key.minor) 1 else 0)))
        events.add(Ev(0, 1, byteArrayOf((0xC0 or channel).toByte(), program.toByte())))
        for (n in score.notes) {
            val on = n.startStep * ticksPerStep
            val off = n.endStep * ticksPerStep
            // 같은 tick이면 note-off(order 2)가 note-on(order 3)보다 먼저
            events.add(Ev(on, 3, byteArrayOf((0x90 or channel).toByte(), n.midi.toByte(), n.velocity.coerceIn(1, 127).toByte())))
            events.add(Ev(off, 2, byteArrayOf((0x80 or channel).toByte(), n.midi.toByte(), 0)))
        }
        events.sortWith(compareBy<Ev> { it.tick }.thenBy { it.order })

        val track = ByteArrayOutputStream()
        var last = 0
        for (e in events) {
            writeVarLen(track, e.tick - last)
            track.write(e.bytes)
            last = e.tick
        }
        writeVarLen(track, 0)
        track.write(byteArrayOf(0xFF.toByte(), 0x2F, 0x00))
        val trackBytes = track.toByteArray()

        val out = ByteArrayOutputStream()
        out.write("MThd".toByteArray(Charsets.US_ASCII))
        writeInt(out, 6)
        writeShort(out, 0) // format 0
        writeShort(out, 1) // 트랙 수
        writeShort(out, TICKS_PER_QUARTER)
        out.write("MTrk".toByteArray(Charsets.US_ASCII))
        writeInt(out, trackBytes.size)
        out.write(trackBytes)
        return out.toByteArray()
    }

    internal fun writeVarLen(out: ByteArrayOutputStream, value: Int) {
        require(value >= 0)
        var buffer = value and 0x7F
        var v = value ushr 7
        while (v > 0) {
            buffer = (buffer shl 8) or ((v and 0x7F) or 0x80)
            v = v ushr 7
        }
        while (true) {
            out.write(buffer and 0xFF)
            if (buffer and 0x80 != 0) buffer = buffer ushr 8 else break
        }
    }

    private fun writeInt(out: ByteArrayOutputStream, v: Int) {
        out.write(v ushr 24 and 0xFF); out.write(v ushr 16 and 0xFF); out.write(v ushr 8 and 0xFF); out.write(v and 0xFF)
    }

    private fun writeShort(out: ByteArrayOutputStream, v: Int) {
        out.write(v ushr 8 and 0xFF); out.write(v and 0xFF)
    }
}
