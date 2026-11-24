package com.largeblueberry.music.audio

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** mono float 오디오. 샘플 값은 [-1, 1]. */
class PcmAudio(val sampleRate: Int, val samples: FloatArray) {
    val durationSec: Double get() = samples.size.toDouble() / sampleRate
}

/**
 * 16-bit PCM WAV 읽기·쓰기. 녹음 파일 형식이고, 모델 입력을 만들 때도 쓴다.
 * 읽을 때 int16 / 32768 로 변환한다(librosa·soundfile과 같은 값). 스테레오면 평균해 mono로 만든다.
 */
object Wav {
    const val HEADER_SIZE = 44

    fun read(input: InputStream): PcmAudio = read(input.readBytes())

    fun read(bytes: ByteArray): PcmAudio {
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        if (bytes.size < 12 || tag(buf, 0) != "RIFF" || tag(buf, 8) != "WAVE") throw IOException("not a WAV file")
        var pos = 12
        var channels = 0
        var sampleRate = 0
        var bits = 0
        var format = 0
        var dataStart = -1
        var dataSize = 0
        while (pos + 8 <= bytes.size) {
            val id = tag(buf, pos)
            val size = buf.getInt(pos + 4)
            val body = pos + 8
            when (id) {
                "fmt " -> {
                    format = buf.getShort(body).toInt() and 0xffff
                    channels = buf.getShort(body + 2).toInt()
                    sampleRate = buf.getInt(body + 4)
                    bits = buf.getShort(body + 14).toInt()
                }
                "data" -> {
                    dataStart = body
                    // 녹음이 중간에 끊겨 크기가 갱신되지 않은 파일도 읽을 수 있게 파일 끝까지로 자른다
                    dataSize = if (size <= 0 || body + size > bytes.size) bytes.size - body else size
                }
            }
            if (dataStart >= 0 && channels > 0) break
            pos = body + size + (size and 1)
        }
        // 0xFFFE(WAVE_FORMAT_EXTENSIBLE)도 16-bit PCM이면 받는다
        if (format != 1 && format != 0xFFFE) throw IOException("unsupported WAV format $format")
        if (bits != 16) throw IOException("unsupported bits per sample $bits")
        if (channels <= 0 || sampleRate <= 0 || dataStart < 0) throw IOException("malformed WAV")

        val frameCount = dataSize / (2 * channels)
        val out = FloatArray(frameCount)
        var p = dataStart
        for (i in 0 until frameCount) {
            var acc = 0f
            for (c in 0 until channels) {
                acc += buf.getShort(p) / 32768f
                p += 2
            }
            out[i] = if (channels == 1) acc else acc / channels
        }
        return PcmAudio(sampleRate, out)
    }

    /** 16-bit mono WAV 헤더. 녹음은 헤더를 먼저 쓰고 끝날 때 크기를 다시 쓴다. */
    fun header(sampleRate: Int, dataBytes: Int): ByteArray {
        val b = ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        b.put("RIFF".toByteArray(Charsets.US_ASCII))
        b.putInt(36 + dataBytes)
        b.put("WAVE".toByteArray(Charsets.US_ASCII))
        b.put("fmt ".toByteArray(Charsets.US_ASCII))
        b.putInt(16)
        b.putShort(1) // PCM
        b.putShort(1) // mono
        b.putInt(sampleRate)
        b.putInt(sampleRate * 2) // byte rate
        b.putShort(2) // block align
        b.putShort(16)
        b.put("data".toByteArray(Charsets.US_ASCII))
        b.putInt(dataBytes)
        return b.array()
    }

    fun encode(audio: PcmAudio): ByteArray {
        val out = ByteArrayOutputStream(HEADER_SIZE + audio.samples.size * 2)
        out.write(header(audio.sampleRate, audio.samples.size * 2))
        val pcm = ByteBuffer.allocate(audio.samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (s in audio.samples) pcm.putShort(floatToPcm16(s))
        out.write(pcm.array())
        return out.toByteArray()
    }

    fun floatToPcm16(s: Float): Short =
        Math.round(s.coerceIn(-1f, 1f) * 32767f).toShort()

    private fun tag(buf: ByteBuffer, at: Int): String =
        String(ByteArray(4) { buf.get(at + it) }, Charsets.US_ASCII)
}
