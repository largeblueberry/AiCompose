package com.largeblueberry.music.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 녹음 샘플레이트를 모델 입력(22,050Hz)으로 맞춘다.
 * 기기가 22,050Hz 녹음을 거부하면 44,100Hz(모든 기기 보장)로 녹음하고 여기서 2배 줄인다(docs/map/notes.md "녹음").
 */
object Resampler {

    private const val TAPS = 63

    /** Blackman 창 sinc 저역통과. 차단 = 0.225 × 샘플레이트(44.1k에서 약 9.9kHz). 대칭 필터라 위상 지연이 없다. */
    private val KERNEL: DoubleArray by lazy {
        val cutoff = 0.225 // 샘플당 주기(0.5 = 나이퀴스트)
        val mid = (TAPS - 1) / 2
        val k = DoubleArray(TAPS) { i ->
            val n = i - mid
            val sinc = if (n == 0) 2 * cutoff else sin(2 * PI * cutoff * n) / (PI * n)
            val window = 0.42 - 0.5 * cos(2 * PI * i / (TAPS - 1)) + 0.08 * cos(4 * PI * i / (TAPS - 1))
            sinc * window
        }
        val sum = k.sum()
        DoubleArray(TAPS) { k[it] / sum }
    }

    /** 저역통과 후 짝수 번째 샘플만 남긴다. 출력 길이 = ceil(n / 2). 가장자리는 0으로 채운 것으로 본다. */
    fun decimateBy2(input: FloatArray): FloatArray {
        val mid = (TAPS - 1) / 2
        val out = FloatArray((input.size + 1) / 2)
        for (o in out.indices) {
            val center = o * 2
            var acc = 0.0
            for (t in 0 until TAPS) {
                val idx = center + t - mid
                if (idx >= 0 && idx < input.size) acc += KERNEL[t] * input[idx]
            }
            out[o] = acc.toFloat()
        }
        return out
    }

    /** 지원하는 변환: 같은 레이트(그대로), 2배(decimateBy2). 그 밖은 녹음 단계에서 막는다. */
    fun toRate(audio: PcmAudio, targetRate: Int): PcmAudio = when (audio.sampleRate) {
        targetRate -> audio
        targetRate * 2 -> PcmAudio(targetRate, decimateBy2(audio.samples))
        else -> throw IllegalArgumentException("unsupported resample ${audio.sampleRate} -> $targetRate")
    }
}
