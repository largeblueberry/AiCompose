package com.largeblueberry.music.basicpitch

import com.largeblueberry.music.basicpitch.BasicPitchConstants.ANNOTATIONS_FPS
import com.largeblueberry.music.basicpitch.BasicPitchConstants.ANNOT_N_FRAMES
import com.largeblueberry.music.basicpitch.BasicPitchConstants.AUDIO_N_SAMPLES
import com.largeblueberry.music.basicpitch.BasicPitchConstants.HOP_SIZE
import com.largeblueberry.music.basicpitch.BasicPitchConstants.N_OVERLAPPING_FRAMES
import com.largeblueberry.music.basicpitch.BasicPitchConstants.OVERLAP_LEN
import com.largeblueberry.music.basicpitch.BasicPitchConstants.SAMPLE_RATE
import kotlin.math.floor

/**
 * 모델 입력 창 분할과 출력 이어 붙이기. 원본: basic_pitch/inference.py
 * `get_audio_input` / `window_audio_file` / `unwrap_output`.
 */
object AudioWindowing {

    /**
     * 22,050Hz mono 오디오를 모델 입력 창들로 나눈다.
     * 앞에 OVERLAP_LEN/2 만큼 0을 붙이고, HOP_SIZE 간격으로 AUDIO_N_SAMPLES 길이씩 자른다(모자라면 0).
     */
    fun windows(audio: FloatArray): List<FloatArray> {
        val pad = OVERLAP_LEN / 2
        val paddedLength = pad + audio.size
        val result = ArrayList<FloatArray>()
        var start = 0
        while (start < paddedLength) {
            val window = FloatArray(AUDIO_N_SAMPLES)
            for (j in 0 until AUDIO_N_SAMPLES) {
                val p = start + j
                if (p >= paddedLength) break
                val src = p - pad
                if (src >= 0) window[j] = audio[src]
            }
            result.add(window)
            start += HOP_SIZE
        }
        return result
    }

    /** 원본 오디오 길이(샘플)에 해당하는 출력 프레임 수. 원본과 같은 식으로 계산한다. */
    fun numOutputFrames(audioLength: Int): Int =
        floor(audioLength * (ANNOTATIONS_FPS.toDouble() / SAMPLE_RATE.toDouble())).toInt()

    /**
     * 창별 출력([ANNOT_N_FRAMES, numBins])을 이어 하나의 행렬로 만든다.
     * 창마다 앞뒤 N_OVERLAPPING_FRAMES/2 프레임을 버리고, 원본 오디오 길이에 맞춰 자른다.
     */
    fun unwrap(windowOutputs: List<FloatArray>, numBins: Int, audioLength: Int): Posteriorgram {
        val olap = N_OVERLAPPING_FRAMES / 2
        val keep = ANNOT_N_FRAMES - 2 * olap
        val total = numOutputFrames(audioLength).coerceAtMost(windowOutputs.size * keep)
        val out = FloatArray(total * numBins)
        var frame = 0
        loop@ for (w in windowOutputs) {
            require(w.size == ANNOT_N_FRAMES * numBins) { "window output size ${w.size}" }
            for (f in olap until ANNOT_N_FRAMES - olap) {
                if (frame >= total) break@loop
                System.arraycopy(w, f * numBins, out, frame * numBins, numBins)
                frame++
            }
        }
        return Posteriorgram(total, numBins, out)
    }
}
