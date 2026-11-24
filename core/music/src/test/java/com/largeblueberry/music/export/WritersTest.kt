package com.largeblueberry.music.export

import com.largeblueberry.music.Golden
import com.largeblueberry.music.score.Key
import com.largeblueberry.music.score.Score
import com.largeblueberry.music.score.ScoreNote
import com.largeblueberry.music.transcription.HummingTranscriber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import javax.sound.midi.MidiSystem
import javax.sound.midi.ShortMessage
import javax.xml.parsers.DocumentBuilderFactory

class WritersTest {

    private fun score(vararg notes: ScoreNote, key: Key = Key.C_MAJOR, bpm: Int = 100) = Score(bpm, key, notes.toList())

    private fun parse(xml: String): org.w3c.dom.Document {
        val f = DocumentBuilderFactory.newInstance()
        // DOCTYPE의 외부 DTD를 내려받지 않게 한다
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        return f.newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray()))
    }

    private fun Element.child(name: String): Element? =
        (0 until childNodes.length).map { childNodes.item(it) }.filterIsInstance<Element>().firstOrNull { it.tagName == name }

    private fun Element.children(name: String): List<Element> =
        (0 until childNodes.length).map { childNodes.item(it) }.filterIsInstance<Element>().filter { it.tagName == name }

    /** 모든 마디의 길이 합이 16이고, 붙임줄 start/stop 짝이 맞는지. */
    private fun assertWellFormed(xml: String) {
        val doc = parse(xml)
        val measures = doc.getElementsByTagName("measure")
        var openTie = false
        for (i in 0 until measures.length) {
            val m = measures.item(i) as Element
            val total = m.children("note").sumOf { it.child("duration")!!.textContent.toInt() }
            assertEquals("measure ${i + 1} duration", 16, total)
            for (n in m.children("note")) {
                val ties = n.children("tie").map { it.getAttribute("type") }
                if ("stop" in ties) { assertTrue("stop without start", openTie); openTie = false }
                if ("start" in ties) { assertTrue("start while open", !openTie); openTie = true }
            }
        }
        assertTrue("dangling tie", !openTie)
    }

    @Test
    fun layoutSplitsAtBarAndOffbeats() {
        assertEquals(listOf(0 to 16), ScoreLayout.split(0, 16))
        assertEquals(listOf(12 to 4, 16 to 4), ScoreLayout.split(12, 8)) // 마디선에서 나눔
        assertEquals(listOf(2 to 2, 4 to 4), ScoreLayout.split(2, 6)) // 박 중간 → 다음 박까지 먼저
        assertEquals(listOf(4 to 4, 8 to 2), ScoreLayout.split(4, 6)) // 2박 시작 점4분은 가운데를 넘지 않게
        assertEquals(listOf(4 to 12), ScoreLayout.split(4, 12)) // 마디 끝까지 채우는 점2분은 허용
        assertEquals(listOf(0 to 16, 16 to 16, 32 to 1), ScoreLayout.split(0, 33))
    }

    @Test
    fun musicXmlTiesAcrossBarline() {
        val xml = MusicXmlWriter.write(score(ScoreNote(67, 12, 8, 100)))
        assertWellFormed(xml)
        val notes = parse(xml).getElementsByTagName("note")
        val pitched = (0 until notes.length).map { notes.item(it) as Element }.filter { it.child("pitch") != null }
        assertEquals(2, pitched.size)
        assertEquals(listOf("start"), pitched[0].children("tie").map { it.getAttribute("type") })
        assertEquals(listOf("stop"), pitched[1].children("tie").map { it.getAttribute("type") })
    }

    @Test
    fun musicXmlAccidentalsFollowKeyAndMeasure() {
        // G장조: F#은 조표에 있어 임시표 없음, F♮은 natural, 같은 마디 다음 F#은 다시 sharp
        val s = score(
            ScoreNote(66, 0, 4, 90), ScoreNote(65, 4, 4, 90), ScoreNote(66, 8, 4, 90),
            ScoreNote(66, 16, 4, 90), // 다음 마디: 상태 초기화 → 임시표 없음
            key = Key(1, false),
        )
        val xml = MusicXmlWriter.write(s)
        assertWellFormed(xml)
        val notes = parse(xml).getElementsByTagName("note")
        val acc = (0 until notes.length).map { notes.item(it) as Element }
            .filter { it.child("pitch") != null }
            .map { it.child("accidental")?.textContent }
        assertEquals(listOf(null, "natural", "sharp", null), acc)
        assertTrue(xml.contains("<fifths>1</fifths>"))
    }

    @Test
    fun musicXmlEmptyScoreIsOneRestMeasure() {
        val xml = MusicXmlWriter.write(score())
        assertWellFormed(xml)
        assertTrue(xml.contains("""<rest measure="yes"/>"""))
    }

    @Test
    fun musicXmlLowVoiceUsesOctaveTrebleClef() {
        val xml = MusicXmlWriter.write(score(ScoreNote(48, 0, 4, 90), ScoreNote(55, 4, 4, 90)))
        assertTrue(xml.contains("<clef-octave-change>-1</clef-octave-change>"))
    }

    @Test
    fun musicXmlEscapesTitle() {
        val xml = MusicXmlWriter.write(score(), title = "A & B <test>")
        assertTrue(xml.contains("A &amp; B &lt;test&gt;"))
        parse(xml)
    }

    @Test
    fun musicXmlForAllGoldenCasesIsWellFormed() {
        for (case in Golden.CASES) {
            val bpm = Golden.truth(case).bpm
            val s = HummingTranscriber().transcribe(Golden.output(case), bpm).score
            assertWellFormed(MusicXmlWriter.write(s, title = case))
        }
    }

    @Test
    fun midiRoundTripsThroughJavaSound() {
        val s = score(ScoreNote(60, 0, 4, 80), ScoreNote(64, 4, 2, 90), ScoreNote(67, 8, 8, 100), bpm = 120)
        val seq = MidiSystem.getSequence(ByteArrayInputStream(MidiWriter.write(s)))
        assertEquals(MidiWriter.TICKS_PER_QUARTER, seq.resolution)
        val track = seq.tracks.single()
        val ons = (0 until track.size()).map { track.get(it) }
            .filter { (it.message as? ShortMessage)?.command == ShortMessage.NOTE_ON }
            .map { Triple(it.tick, (it.message as ShortMessage).data1, (it.message as ShortMessage).data2) }
        assertEquals(listOf(Triple(0L, 60, 80), Triple(480L, 64, 90), Triple(960L, 67, 100)), ons)
        // 템포 메타(0x51): 120bpm = 500000us
        val tempo = (0 until track.size()).map { track.get(it).message }
            .filterIsInstance<javax.sound.midi.MetaMessage>().first { it.type == 0x51 }.data
        assertEquals(500_000, (tempo[0].toInt() and 0xFF shl 16) or (tempo[1].toInt() and 0xFF shl 8) or (tempo[2].toInt() and 0xFF))
        assertEquals(16L * 120, seq.tickLength)
    }

    @Test
    fun varLenEncoding() {
        fun enc(v: Int) = java.io.ByteArrayOutputStream().also { MidiWriter.writeVarLen(it, v) }.toByteArray()
            .joinToString(" ") { "%02X".format(it) }
        assertEquals("00", enc(0))
        assertEquals("7F", enc(0x7F))
        assertEquals("81 00", enc(0x80))
        assertEquals("FF 7F", enc(0x3FFF))
        assertEquals("81 80 00", enc(0x4000))
    }
}
