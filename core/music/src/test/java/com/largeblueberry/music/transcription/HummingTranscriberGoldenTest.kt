package com.largeblueberry.music.transcription

import com.largeblueberry.music.Golden
import com.largeblueberry.music.score.Key
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * 합성 허밍(정답 악보를 알고 있음)의 Basic Pitch 출력으로 전체 후처리를 돌려 정답 악보와 비교한다.
 * 실제 허밍은 이보다 지저분하다. 이 테스트는 "깨끗한 입력에서는 정확히 맞아야 한다"는 하한선이다.
 */
class HummingTranscriberGoldenTest {

    private val transcriber: ScoreTranscriber = HummingTranscriber()

    private fun check(case: String) {
        val truth = Golden.truth(case)
        val result = transcriber.transcribe(Golden.output(case), truth.bpm)
        val spb = result.score.stepsPerBeat
        val expected = truth.notes.map { Triple(it.midi, (it.startBeat * spb).toInt(), (it.beats * spb).toInt()) }
        val actual = result.score.notes.map { Triple(it.midi, it.startStep, it.lengthSteps) }
        assertEquals("$case notes (midi, startStep, lengthSteps)", expected, actual)
        // 글리산도가 있으면 onset이 늦게 잡혀 위상도 그만큼 늦다. 격자 한 칸의 1/4 안이면 된다.
        assertTrue(
            "$case phase ${result.phaseSec} vs offset ${truth.startOffsetSec}",
            abs(result.phaseSec - truth.startOffsetSec) < result.score.stepSeconds() / 4,
        )
        truth.key?.let { assertEquals("$case key", it, result.score.key.name) }
    }

    @Test fun cMajor100() = check("c_major_100bpm")

    @Test fun gMajor80Legato() = check("g_major_80bpm_legato")

    @Test fun short1s() = check("short_1s")

    /**
     * 박자·길이·음정이 흐트러지고 지연이 큰(0.32초) 경우. 정확히 같기를 기대하지 않고 음 단위로 센다.
     * onset 일치 = (음높이, 시작 step)이 같음. 완전 일치 = 길이까지 같음.
     */
    @Test
    fun humanizedIsMostlyRight() {
        val case = "humanized_d_major_90bpm"
        val truth = Golden.truth(case)
        val result = transcriber.transcribe(Golden.output(case), truth.bpm)
        val spb = result.score.stepsPerBeat
        val expected = truth.notes.map { Triple(it.midi, (it.startBeat * spb).toInt(), (it.beats * spb).toInt()) }
        val actual = result.score.notes.map { Triple(it.midi, it.startStep, it.lengthSteps) }
        val onsetHits = expected.count { e -> actual.any { it.first == e.first && it.second == e.second } }
        val exactHits = expected.count { it in actual }
        val extra = actual.count { a -> expected.none { it.first == a.first && it.second == a.second } }
        println("$case phase=${result.phaseSec} key=${result.score.key.name}")
        println("expected=$expected")
        println("actual  =$actual")
        println("onsetHits=$onsetHits exactHits=$exactHits extra=$extra / ${expected.size}")
        assertTrue("onset hits $onsetHits/${expected.size}", onsetHits >= expected.size * 0.9)
        assertTrue("exact hits $exactHits/${expected.size}", exactHits >= expected.size * 0.75)
        assertTrue("extra notes $extra", extra <= 2)
        assertEquals("D major", result.score.key.name)
    }

    @Test
    fun paramsGivenAtConstructionAreUsed() {
        // 조를 정해 주면 자동 추정 대신 그 조를 쓴다
        val fixed = HummingTranscriber(TranscriptionParams(key = Key(-3, true)))
        val result = fixed.transcribe(Golden.output("c_major_100bpm"), 100)
        assertEquals("C minor", result.score.key.name)
    }

    @Test
    fun quietNoiseGivesEmptyScore() {
        val result = transcriber.transcribe(Golden.output("quiet_noise"), 100)
        assertEquals(0, result.score.notes.size)
        assertEquals(1, result.score.barCount)
    }
}
