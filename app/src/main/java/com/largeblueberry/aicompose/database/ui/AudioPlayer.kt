package com.largeblueberry.aicompose.database.ui

import android.media.MediaPlayer
import android.util.Log

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null

    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    fun play(filePath: String) {
        try {
            // 이전 MediaPlayer가 재생 중이라면 정지 후 해제
            mediaPlayer?.stop()
            mediaPlayer?.release()

            // 새로운 MediaPlayer 초기화
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath) // 녹음 파일 경로 설정
                prepare() // 파일 준비
                setOnCompletionListener {
                    onCompletionListener?.invoke() // 재생 완료 시 ViewModel에 알림
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio file: ${e.message}")
            onCompletionListener?.invoke() // 에러 발생 시에도 상태 초기화를 위해 알림
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

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
}