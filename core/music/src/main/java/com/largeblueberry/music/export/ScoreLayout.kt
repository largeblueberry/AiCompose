package com.largeblueberry.music.export

/**
 * 악보에 그릴 수 있는 조각으로 음·쉼표를 나눈다(4/4, 16분음표 격자 전용).
 * - 마디선을 넘으면 나누고 붙임줄로 잇는다.
 * - 박 중간에서 시작하면 다음 박까지 먼저 채운다.
 * - 2·4박에서 시작한 음은 마디 가운데(3박)를 넘지 않게 나눈다. 단, 마디 끝까지 채우는 점2분음표는 허용.
 * 쓸 수 있는 길이(step): 16 온, 12 점2분, 8 2분, 6 점4분, 4 4분, 3 점8분, 2 8분, 1 16분.
 */
internal object ScoreLayout {

    const val STEPS_PER_BEAT = 4
    const val STEPS_PER_BAR = 16

    data class Duration(val steps: Int, val type: String, val dotted: Boolean)

    private val DURATIONS = listOf(
        Duration(16, "whole", false),
        Duration(12, "half", true),
        Duration(8, "half", false),
        Duration(6, "quarter", true),
        Duration(4, "quarter", false),
        Duration(3, "eighth", true),
        Duration(2, "eighth", false),
        Duration(1, "16th", false),
    )

    fun duration(steps: Int): Duration = DURATIONS.first { it.steps == steps }

    /** [start, start+length) 를 그릴 수 있는 조각(시작 step, 길이)들로 나눈다. */
    fun split(start: Int, length: Int): List<Pair<Int, Int>> {
        val out = ArrayList<Pair<Int, Int>>()
        var pos = start
        var remain = length
        while (remain > 0) {
            val inBar = Math.floorMod(pos, STEPS_PER_BAR)
            val toBarEnd = STEPS_PER_BAR - inBar
            val limit = minOf(remain, toBarEnd)
            val chunk = if (inBar % STEPS_PER_BEAT != 0) {
                minOf(limit, STEPS_PER_BEAT - inBar % STEPS_PER_BEAT)
            } else {
                DURATIONS.first { d ->
                    d.steps <= limit && allowedOnBeat(inBar, d.steps)
                }.steps
            }
            out.add(pos to chunk)
            pos += chunk
            remain -= chunk
        }
        return out
    }

    private fun allowedOnBeat(inBar: Int, steps: Int): Boolean {
        val half = STEPS_PER_BAR / 2
        if (inBar % half == 0) return true // 1박·3박
        val crossesMiddle = inBar < half && inBar + steps > half
        val fillsBar = inBar + steps == STEPS_PER_BAR
        return !crossesMiddle || fillsBar
    }
}
