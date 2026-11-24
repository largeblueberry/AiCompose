package com.largeblueberry.music.score

/**
 * 조성. 조표(fifths: 플랫 -7 … 샵 +7)와 장·단조로 나타낸다. 음이름 표기(♯/♭)도 여기서 정한다.
 */
data class Key(val fifths: Int, val minor: Boolean) {
    init {
        require(fifths in -7..7) { "fifths out of range: $fifths" }
    }

    /** 같은 조표의 장조 으뜸음 글자 인덱스(C=0 … B=6)와 변화. */
    private val majorTonic: Spelled get() = MAJOR_TONICS[fifths + 7]

    private val tonic: Spelled
        get() = if (!minor) majorTonic else {
            // 나란한조 단조 으뜸음 = 장조 으뜸음의 6음(글자 +5, 단3도 아래)
            val letter = (majorTonic.letter + 5) % 7
            val pc = Math.floorMod(majorTonic.pc - 3, 12)
            Spelled(letter, alterFor(letter, pc))
        }

    val tonicPitchClass: Int get() = tonic.pc

    /** 예: "C major", "F# minor", "Bb major" */
    val name: String get() = tonic.text + if (minor) " minor" else " major"

    /** 조표에 맞는 음이름. 조 안의 음은 음계대로, 조 밖의 음은 샵 조면 ♯, 플랫 조면 ♭으로 적는다. */
    fun spell(midi: Int): Pitch {
        val pc = Math.floorMod(midi, 12)
        val diatonic = scale().firstOrNull { it.pc == pc }
        val s = diatonic ?: chromatic(pc)
        val octave = Math.floorDiv(midi - s.alter - NATURAL_PC[s.letter], 12) - 1
        return Pitch(LETTERS[s.letter], s.alter, octave)
    }

    private fun scale(): List<Spelled> {
        val t = majorTonic
        val steps = intArrayOf(0, 2, 4, 5, 7, 9, 11)
        return (0 until 7).map { i ->
            val letter = (t.letter + i) % 7
            Spelled(letter, alterFor(letter, t.pc + steps[i]))
        }
    }

    /** 조 밖의 음: 자연음이면 그대로(♮), 아니면 샵 조는 아래 글자 + ♯, 플랫 조는 위 글자 + ♭. */
    private fun chromatic(pc: Int): Spelled {
        val natural = NATURAL_PC.indexOf(pc)
        if (natural >= 0) return Spelled(natural, 0)
        val neighbor = if (fifths >= 0) Math.floorMod(pc - 1, 12) else Math.floorMod(pc + 1, 12)
        val letter = NATURAL_PC.indexOf(neighbor)
        return Spelled(letter, alterFor(letter, pc))
    }

    private data class Spelled(val letter: Int, val alter: Int) {
        val pc: Int get() = Math.floorMod(NATURAL_PC[letter] + alter, 12)
        val text: String get() = LETTERS[letter] + when (alter) {
            -2 -> "bb"; -1 -> "b"; 1 -> "#"; 2 -> "##"; else -> ""
        }
    }

    companion object {
        val C_MAJOR = Key(0, false)
        internal val LETTERS = arrayOf("C", "D", "E", "F", "G", "A", "B")
        internal val NATURAL_PC = intArrayOf(0, 2, 4, 5, 7, 9, 11)

        // fifths -7..7 의 장조 으뜸음: Cb Gb Db Ab Eb Bb F C G D A E B F# C#
        private val MAJOR_TONICS = listOf(
            Spelled(0, -1), Spelled(4, -1), Spelled(1, -1), Spelled(5, -1), Spelled(2, -1), Spelled(6, -1),
            Spelled(3, 0), Spelled(0, 0), Spelled(4, 0), Spelled(1, 0), Spelled(5, 0), Spelled(2, 0),
            Spelled(6, 0), Spelled(3, 1), Spelled(0, 1),
        )

        private fun alterFor(letter: Int, pc: Int): Int {
            var d = Math.floorMod(pc - NATURAL_PC[letter], 12)
            if (d > 6) d -= 12
            return d
        }

        /**
         * 으뜸음 pitch class와 장·단조로 조를 만든다. 같은 소리의 두 조표(예: F#/Gb)는 조표 수가 적은 쪽,
         * 같으면 플랫 쪽을 고른다(Db major, Gb major 처럼 흔히 쓰는 표기).
         */
        fun of(tonicPc: Int, minor: Boolean): Key {
            val candidates = (-7..7).map { Key(it, minor) }.filter { it.tonicPitchClass == Math.floorMod(tonicPc, 12) }
            return candidates.minWith(compareBy<Key> { kotlin.math.abs(it.fifths) }.thenBy { it.fifths })
        }
    }
}

/** 악보용 음이름. step은 "C".."B", alter는 -2..2, octave는 과학적 표기(C4 = 가운데 도). */
data class Pitch(val step: String, val alter: Int, val octave: Int)
