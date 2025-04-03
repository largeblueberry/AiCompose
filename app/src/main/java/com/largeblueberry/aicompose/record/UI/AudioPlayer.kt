package com.largeblueberry.aicompose.record.UI

import android.media.MediaPlayer
import android.util.Log

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun play(filePath: String) {
        try {
            // 이전 MediaPlayer가 재생 중이라면 정지 후 해제
            mediaPlayer?.stop()
            mediaPlayer?.release()

            // 새로운 MediaPlayer 초기화
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath) // 녹음 파일 경로 설정
                prepare() // 파일 준비
                start() // 파일 재생
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio file: ${e.message}")
        }
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
