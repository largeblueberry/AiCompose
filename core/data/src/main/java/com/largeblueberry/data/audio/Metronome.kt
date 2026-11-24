package com.largeblueberry.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 클릭 메트로놈. 클릭 PCM을 AudioTrack 스트림에 샘플 단위로 이어 써서 박 간격이 흔들리지 않게 한다
 * (SoundPool은 재생 시작 시각이 흔들린다, docs/map/notes.md).
 *
 * - 처음 [countInBeats] 박은 항상 소리를 낸다(카운트인).
 * - 그 뒤는 [clickAfterCountIn]이 true일 때만 계속 낸다. 스피커 클릭이 마이크로 들어가 음표로 잡히지 않게,
 *   이어폰이 연결됐을 때만 true로 쓴다.
 * - 마디 첫 박은 높은 소리로 강조한다.
 */
internal class Metronome(
    private val bpm: Int,
    private val beatsPerBar: Int,
    private val countInBeats: Int,
    private val clickAfterCountIn: Boolean,
) {
    @Volatile private var running = false
    private var thread: Thread? = null
    private var track: AudioTrack? = null

    fun start() {
        check(!running) { "already running" }
        val bufferBytes = maxOf(
            AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT),
            SAMPLE_RATE / 10 * 2,
        )
        val t = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferBytes)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        track = t
        running = true
        t.play()
        thread = Thread({ loop(t) }, "metronome").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    private fun loop(t: AudioTrack) {
        val samplesPerBeat = (SAMPLE_RATE * 60.0 / bpm).roundToInt()
        val accent = click(ACCENT_HZ, samplesPerBeat)
        val normal = click(NORMAL_HZ, samplesPerBeat)
        var beat = 0
        try {
            while (running) {
                if (beat >= countInBeats && !clickAfterCountIn) break
                val buf = if (beat % beatsPerBar == 0) accent else normal
                var off = 0
                // write는 버퍼가 빌 때까지 막히므로 재생 속도에 맞춰 돈다
                while (running && off < buf.size) {
                    val n = t.write(buf, off, buf.size - off)
                    if (n < 0) {
                        Log.w(TAG, "AudioTrack.write error $n")
                        return
                    }
                    off += n
                }
                beat++
            }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "metronome stopped", e)
        }
    }

    fun stop() {
        running = false
        track?.let {
            try {
                it.pause()
                it.flush()
            } catch (_: IllegalStateException) {
            }
        }
        thread?.join(500)
        thread = null
        track?.release()
        track = null
    }

    companion object {
        private const val TAG = "Metronome"
        const val SAMPLE_RATE = 44_100
        private const val ACCENT_HZ = 1_600.0
        private const val NORMAL_HZ = 1_000.0
        private const val CLICK_SEC = 0.03

        /** 한 박 길이의 버퍼: 앞 30ms 감쇠 사인 클릭 + 나머지 무음 */
        internal fun click(freq: Double, samplesPerBeat: Int): ShortArray {
            val out = ShortArray(samplesPerBeat)
            val n = minOf(samplesPerBeat, (SAMPLE_RATE * CLICK_SEC).toInt())
            for (i in 0 until n) {
                val t = i.toDouble() / SAMPLE_RATE
                val env = exp(-t / 0.008)
                out[i] = (0.6 * env * sin(2 * PI * freq * t) * Short.MAX_VALUE).toInt().toShort()
            }
            return out
        }
    }
}
