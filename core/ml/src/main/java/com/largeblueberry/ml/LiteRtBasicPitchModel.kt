package com.largeblueberry.ml

import android.content.Context
import com.largeblueberry.music.basicpitch.AudioWindowing
import com.largeblueberry.music.basicpitch.BasicPitchConstants
import com.largeblueberry.music.basicpitch.BasicPitchOutput
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Spotify Basic Pitch(nmp.tflite, Apache 2.0)를 LiteRT로 실행한다.
 *
 * 모델 입출력(tools/basic_pitch_golden 로 확인):
 * - 시그니처 "serving_default", 입력 "input_2" [1, 43844, 1] float32
 * - 출력 "note" [1, 172, 88], "onset" [1, 172, 88], "contour" [1, 172, 264]
 * - 출력 텐서 인덱스 순서가 이름 순서와 다르므로 반드시 시그니처 이름으로 접근한다.
 * - 모든 연산이 builtin이라 Flex delegate가 필요 없다. 작은 모델이라 CPU(XNNPACK)로 돌린다.
 *
 * Interpreter는 스레드 안전하지 않아 predict를 동기화한다.
 * 네이티브 자원이라 Closeable이다. 앱에서는 DI 싱글턴으로 하나만 열고 프로세스와 같이 산다(닫지 않는다).
 */
class LiteRtBasicPitchModel(modelBytes: ByteBuffer, numThreads: Int = 2) : BasicPitchModel, Closeable {

    private val interpreter = Interpreter(modelBytes, Interpreter.Options().setNumThreads(numThreads))

    private val input = direct(BasicPitchConstants.AUDIO_N_SAMPLES)
    private val note = direct(BasicPitchConstants.ANNOT_N_FRAMES * BasicPitchConstants.N_PITCHES)
    private val onset = direct(BasicPitchConstants.ANNOT_N_FRAMES * BasicPitchConstants.N_PITCHES)
    private val contour = direct(BasicPitchConstants.ANNOT_N_FRAMES * BasicPitchConstants.N_CONTOUR_BINS)

    /** 창마다 모델을 한 번씩 돌리고(:core:music의 창 분할), 결과를 이어 붙인다. */
    @Synchronized
    override fun predict(audio: FloatArray, onProgress: (done: Int, total: Int) -> Unit): BasicPitchOutput {
        val windows = AudioWindowing.windows(audio)
        val notes = ArrayList<FloatArray>(windows.size)
        val onsets = ArrayList<FloatArray>(windows.size)
        windows.forEachIndexed { i, w ->
            input.clear()
            input.asFloatBuffer().put(w)
            note.clear(); onset.clear(); contour.clear()
            interpreter.runSignature(
                mapOf(INPUT to input),
                mapOf(NOTE to note, ONSET to onset, CONTOUR to contour),
                SIGNATURE,
            )
            notes.add(read(note))
            onsets.add(read(onset))
            onProgress(i + 1, windows.size)
        }
        val n = BasicPitchConstants.N_PITCHES
        return BasicPitchOutput(
            note = AudioWindowing.unwrap(notes, n, audio.size),
            onset = AudioWindowing.unwrap(onsets, n, audio.size),
        )
    }

    @Synchronized
    override fun close() = interpreter.close()

    private fun read(buf: ByteBuffer): FloatArray {
        buf.rewind()
        val fb = buf.asFloatBuffer()
        return FloatArray(fb.remaining()).also { fb.get(it) }
    }

    companion object {
        const val ASSET_PATH = "basic_pitch/nmp.tflite"
        private const val SIGNATURE = "serving_default"
        private const val INPUT = "input_2"
        private const val NOTE = "note"
        private const val ONSET = "onset"
        private const val CONTOUR = "contour"

        /**
         * assets에서 모델을 읽는다. 압축 여부와 상관없이 동작하도록 mmap 대신 direct 버퍼로 복사한다(약 200KB).
         */
        fun fromAssets(context: Context, numThreads: Int = 2): LiteRtBasicPitchModel {
            val bytes = context.assets.open(ASSET_PATH).use { it.readBytes() }
            val buf = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
            buf.put(bytes).rewind()
            return LiteRtBasicPitchModel(buf, numThreads)
        }

        private fun direct(floats: Int): ByteBuffer =
            ByteBuffer.allocateDirect(floats * 4).order(ByteOrder.nativeOrder())
    }
}
