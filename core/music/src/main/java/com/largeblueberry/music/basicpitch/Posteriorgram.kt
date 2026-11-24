package com.largeblueberry.music.basicpitch

/**
 * 프레임 × bin 확률 행렬. 행 우선(row-major) [frame * numBins + bin].
 * Basic Pitch의 note/onset 출력(bin 88개)을 담는다.
 */
class Posteriorgram(
    val numFrames: Int,
    val numBins: Int,
    val values: FloatArray,
) {
    init {
        require(numFrames >= 0 && numBins > 0) { "bad shape $numFrames x $numBins" }
        require(values.size == numFrames * numBins) {
            "values.size=${values.size} != $numFrames x $numBins"
        }
    }

    operator fun get(frame: Int, bin: Int): Float = values[frame * numBins + bin]

    companion object {
        fun empty(numBins: Int = BasicPitchConstants.N_PITCHES) = Posteriorgram(0, numBins, FloatArray(0))
    }
}

/** 모델 출력 중 후처리에 쓰는 두 행렬. contour(피치 벤드용)는 악보에 필요 없어 받지 않는다. */
class BasicPitchOutput(
    val note: Posteriorgram,
    val onset: Posteriorgram,
) {
    init {
        require(note.numFrames == onset.numFrames && note.numBins == onset.numBins) {
            "note/onset shape mismatch"
        }
    }

    val numFrames: Int get() = note.numFrames
}
