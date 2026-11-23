package com.largeblueberry.music.audio

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class WavTest {

    @Test
    fun encodeThenReadRoundTrips() {
        val samples = FloatArray(1000) { (it % 50 - 25) / 25f * 0.8f }
        val back = Wav.read(Wav.encode(PcmAudio(22_050, samples)))
        assertEquals(22_050, back.sampleRate)
        assertEquals(samples.size, back.samples.size)
        samples.forEachIndexed { i, s -> assertEquals(s, back.samples[i], 1f / 16384) }
    }

    @Test
    fun readsTruncatedDataChunk() {
        // 녹음 중 앱이 죽어 data 크기가 0으로 남은 파일
        val bytes = Wav.encode(PcmAudio(16_000, FloatArray(100) { 0.5f }))
        java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).putInt(40, 0)
        assertEquals(100, Wav.read(bytes).samples.size)
    }

    @Test(expected = IOException::class)
    fun rejectsNonWav() {
        Wav.read(ByteArray(64))
    }
}
