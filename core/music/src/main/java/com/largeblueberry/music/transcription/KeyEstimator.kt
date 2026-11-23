package com.largeblueberry.music.transcription

import com.largeblueberry.music.score.Key
import kotlin.math.sqrt

/**
 * Krumhansl–Schmuckler 조성 추정. 음 길이로 가중한 pitch class 분포를 24개 조의 프로필(Krumhansl–Kessler)과
 * 상관 비교해 가장 높은 조를 고른다. 짧은 선율에서는 틀릴 수 있다. 결과는 조표·음이름 표기에 쓴다.
 */
object KeyEstimator {

    private val MAJOR = doubleArrayOf(6.35, 2.23, 3.48, 2.33, 4.38, 4.09, 2.52, 5.19, 2.39, 3.66, 2.29, 2.88)
    private val MINOR = doubleArrayOf(6.33, 2.68, 3.52, 5.38, 2.60, 3.53, 2.54, 4.75, 3.98, 2.69, 3.34, 3.17)

    /** (midi, 가중치) 목록. 음이 없거나 가중치 합이 0이면 C major. */
    fun estimate(notes: List<Pair<Int, Double>>): Key {
        val hist = DoubleArray(12)
        for ((midi, w) in notes) hist[Math.floorMod(midi, 12)] += w
        if (hist.sum() <= 0.0) return Key.C_MAJOR

        var best = Key.C_MAJOR
        var bestScore = Double.NEGATIVE_INFINITY
        for (minor in listOf(false, true)) {
            val profile = if (minor) MINOR else MAJOR
            for (tonic in 0 until 12) {
                val rotated = DoubleArray(12) { profile[Math.floorMod(it - tonic, 12)] }
                val r = pearson(hist, rotated)
                // 같은 점수면 장조, 낮은 으뜸음을 먼저(순회 순서) 남긴다
                if (r > bestScore + 1e-12) {
                    bestScore = r
                    best = Key.of(tonic, minor)
                }
            }
        }
        return best
    }

    private fun pearson(a: DoubleArray, b: DoubleArray): Double {
        val ma = a.average()
        val mb = b.average()
        var num = 0.0
        var da = 0.0
        var db = 0.0
        for (i in a.indices) {
            num += (a[i] - ma) * (b[i] - mb)
            da += (a[i] - ma) * (a[i] - ma)
            db += (b[i] - mb) * (b[i] - mb)
        }
        val den = sqrt(da * db)
        return if (den == 0.0) 0.0 else num / den
    }
}
