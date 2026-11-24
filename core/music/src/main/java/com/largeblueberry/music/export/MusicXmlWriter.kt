package com.largeblueberry.music.export

import com.largeblueberry.music.score.Key
import com.largeblueberry.music.score.Score
import com.largeblueberry.music.score.ScoreNote

/**
 * Score → MusicXML 4.0 (score-partwise). 앱 안의 악보 렌더러(WebView)와 외부 공유에 쓴다.
 * 성부 하나, 4/4, divisions = 4(16분음표 = 1).
 * 음높이는 실제 소리 기준이고, 낮은 목소리(중앙값 < C4)는 옥타브 아래 높은음자리표(테너용)로 적는다.
 */
object MusicXmlWriter {

    fun write(score: Score, title: String = "Untitled", software: String = "Eaream"): String {
        require(score.beatsPerBar == 4 && score.stepsPerBeat == ScoreLayout.STEPS_PER_BEAT) {
            "only 4/4 with 16th grid is supported"
        }
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="no"?>""").append('\n')
        sb.append("""<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN" "http://www.musicxml.org/dtds/partwise.dtd">""").append('\n')
        sb.append("""<score-partwise version="4.0">""").append('\n')
        sb.append("  <work><work-title>").append(escape(title)).append("</work-title></work>\n")
        sb.append("  <identification><encoding><software>").append(escape(software))
            .append("</software></encoding></identification>\n")
        sb.append("""  <part-list><score-part id="P1"><part-name>Voice</part-name></score-part></part-list>""").append('\n')
        sb.append("""  <part id="P1">""").append('\n')

        val lowVoice = median(score.notes.map { it.midi }) ?.let { it < 60 } ?: false
        val keyAlters = keySignatureAlters(score.key)

        for (bar in 0 until score.barCount) {
            val barStart = bar * ScoreLayout.STEPS_PER_BAR
            val barEnd = barStart + ScoreLayout.STEPS_PER_BAR
            sb.append("""    <measure number="${bar + 1}">""").append('\n')
            if (bar == 0) appendAttributes(sb, score, lowVoice)

            // 이 마디의 임시표 상태: (글자, 옥타브) → 지금 적용 중인 변화
            val accidentalState = HashMap<Pair<String, Int>, Int>()
            val events = barEvents(score, barStart, barEnd)
            if (events.isEmpty()) {
                sb.append("""      <note><rest measure="yes"/><duration>${ScoreLayout.STEPS_PER_BAR}</duration><voice>1</voice></note>""").append('\n')
            }
            for (e in events) {
                if (e.note == null) {
                    appendRest(sb, e.length)
                } else {
                    appendNote(sb, score.key, e, keyAlters, accidentalState)
                }
            }
            sb.append("    </measure>\n")
        }
        sb.append("  </part>\n</score-partwise>\n")
        return sb.toString()
    }

    /** 마디 안 조각. note가 null이면 쉼표. tieStart/tieStop은 같은 음의 앞뒤 조각과 잇는지. */
    private class Event(val start: Int, val length: Int, val note: ScoreNote?, val tieStop: Boolean, val tieStart: Boolean)

    private fun barEvents(score: Score, barStart: Int, barEnd: Int): List<Event> {
        val out = ArrayList<Event>()
        var cursor = barStart
        val inBar = score.notes.filter { it.startStep < barEnd && it.endStep > barStart }
        if (inBar.isEmpty()) return out
        for (n in inBar) {
            if (n.startStep > cursor) {
                ScoreLayout.split(cursor, n.startStep - cursor).forEach { (s, l) -> out.add(Event(s, l, null, false, false)) }
            }
            val pieces = ScoreLayout.split(n.startStep, n.lengthSteps)
            pieces.forEachIndexed { i, (s, l) ->
                if (s >= barStart && s < barEnd) {
                    out.add(Event(s, l, n, tieStop = i > 0, tieStart = i < pieces.size - 1))
                }
            }
            cursor = minOf(n.endStep, barEnd)
        }
        if (cursor < barEnd) {
            ScoreLayout.split(cursor, barEnd - cursor).forEach { (s, l) -> out.add(Event(s, l, null, false, false)) }
        }
        return out
    }

    private fun appendAttributes(sb: StringBuilder, score: Score, lowVoice: Boolean) {
        sb.append("      <attributes>\n")
        sb.append("        <divisions>${ScoreLayout.STEPS_PER_BEAT}</divisions>\n")
        sb.append("        <key><fifths>${score.key.fifths}</fifths><mode>${if (score.key.minor) "minor" else "major"}</mode></key>\n")
        sb.append("        <time><beats>${score.beatsPerBar}</beats><beat-type>4</beat-type></time>\n")
        if (lowVoice) {
            sb.append("        <clef><sign>G</sign><line>2</line><clef-octave-change>-1</clef-octave-change></clef>\n")
        } else {
            sb.append("        <clef><sign>G</sign><line>2</line></clef>\n")
        }
        sb.append("      </attributes>\n")
        sb.append("""      <direction placement="above"><direction-type><metronome><beat-unit>quarter</beat-unit><per-minute>${score.bpm}</per-minute></metronome></direction-type><sound tempo="${score.bpm}"/></direction>""").append('\n')
    }

    private fun appendRest(sb: StringBuilder, length: Int) {
        val d = ScoreLayout.duration(length)
        sb.append("      <note><rest/><duration>$length</duration><voice>1</voice><type>${d.type}</type>")
        if (d.dotted) sb.append("<dot/>")
        sb.append("</note>\n")
    }

    private fun appendNote(
        sb: StringBuilder,
        key: Key,
        e: Event,
        keyAlters: Map<String, Int>,
        state: HashMap<Pair<String, Int>, Int>,
    ) {
        val n = e.note!!
        val p = key.spell(n.midi)
        val d = ScoreLayout.duration(e.length)
        val slot = p.step to p.octave
        val current = state[slot] ?: keyAlters[p.step] ?: 0
        // 붙임줄로 이어진 조각은 임시표를 다시 적지 않는다
        val accidental = if (!e.tieStop && current != p.alter) accidentalName(p.alter) else null
        state[slot] = p.alter

        sb.append("      <note>")
        sb.append("<pitch><step>${p.step}</step>")
        if (p.alter != 0) sb.append("<alter>${p.alter}</alter>")
        sb.append("<octave>${p.octave}</octave></pitch>")
        sb.append("<duration>${e.length}</duration>")
        if (e.tieStop) sb.append("""<tie type="stop"/>""")
        if (e.tieStart) sb.append("""<tie type="start"/>""")
        sb.append("<voice>1</voice><type>${d.type}</type>")
        if (d.dotted) sb.append("<dot/>")
        if (accidental != null) sb.append("<accidental>$accidental</accidental>")
        if (e.tieStop || e.tieStart) {
            sb.append("<notations>")
            if (e.tieStop) sb.append("""<tied type="stop"/>""")
            if (e.tieStart) sb.append("""<tied type="start"/>""")
            sb.append("</notations>")
        }
        sb.append("</note>\n")
    }

    /** 조표가 바꾸는 글자별 변화. */
    private fun keySignatureAlters(key: Key): Map<String, Int> {
        val sharps = listOf("F", "C", "G", "D", "A", "E", "B")
        val flats = sharps.reversed()
        return if (key.fifths >= 0) sharps.take(key.fifths).associateWith { 1 }
        else flats.take(-key.fifths).associateWith { -1 }
    }

    private fun accidentalName(alter: Int) = when (alter) {
        -2 -> "flat-flat"; -1 -> "flat"; 0 -> "natural"; 1 -> "sharp"; 2 -> "double-sharp"
        else -> error("alter $alter")
    }

    private fun median(values: List<Int>): Int? {
        if (values.isEmpty()) return null
        val s = values.sorted()
        return s[s.size / 2]
    }

    private fun escape(text: String) = text
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&apos;")
}
