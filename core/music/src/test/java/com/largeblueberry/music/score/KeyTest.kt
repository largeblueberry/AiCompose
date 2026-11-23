package com.largeblueberry.music.score

import com.largeblueberry.music.transcription.KeyEstimator
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyTest {

    @Test
    fun names() {
        assertEquals("C major", Key(0, false).name)
        assertEquals("A minor", Key(0, true).name)
        assertEquals("F# major", Key(6, false).name)
        assertEquals("Bb major", Key(-2, false).name)
        assertEquals("E minor", Key(1, true).name)
        assertEquals("C# minor", Key(4, true).name)
        assertEquals("Cb major", Key(-7, false).name)
    }

    @Test
    fun ofPrefersFewerAccidentalsThenFlats() {
        assertEquals(Key(-5, false), Key.of(1, false)) // Db, not C#
        assertEquals(Key(-6, false), Key.of(6, false)) // Gb vs F#: tie → flat
        assertEquals(Key(5, false), Key.of(11, false)) // B, not Cb
        assertEquals(Key(4, true), Key.of(1, true)) // C# minor, not Db minor(없음)
    }

    @Test
    fun spellingFollowsKey() {
        val g = Key(1, false)
        assertEquals(Pitch("F", 1, 4), g.spell(66))
        assertEquals(Pitch("F", 0, 4), g.spell(65)) // 조 밖 자연음은 그대로
        assertEquals(Pitch("C", 1, 4), g.spell(61)) // 샵 조의 조 밖 음은 ♯
        val f = Key(-1, false)
        assertEquals(Pitch("B", -1, 4), f.spell(70))
        assertEquals(Pitch("D", -1, 4), f.spell(61)) // 플랫 조의 조 밖 음은 ♭
        val fSharp = Key(6, false)
        assertEquals(Pitch("E", 1, 4), fSharp.spell(65)) // F# 장조의 7음은 E#
        val cFlat = Key(-7, false)
        assertEquals(Pitch("C", -1, 5), cFlat.spell(71)) // B4 소리 = Cb5 표기
        assertEquals(Pitch("C", 0, 4), Key.C_MAJOR.spell(60))
        assertEquals(Pitch("A", 0, 0), Key.C_MAJOR.spell(21))
    }

    @Test
    fun keyEstimatorOnScales() {
        val cMajor = listOf(60, 62, 64, 65, 67, 69, 71, 72, 67, 64, 60).map { it to 1.0 }
        assertEquals("C major", KeyEstimator.estimate(cMajor).name)
        val aMinor = listOf(57, 59, 60, 62, 64, 65, 68, 69, 64, 57, 57, 69).map { it to 1.0 }
        assertEquals("A minor", KeyEstimator.estimate(aMinor).name)
        assertEquals(Key.C_MAJOR, KeyEstimator.estimate(emptyList()))
    }
}
