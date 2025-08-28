package com.largeblueberry.library.util

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
            mediaPlayer = null // 이전 인스턴스 확실히 해제

            // 새로운 MediaPlayer 초기화
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath) // 녹음 파일 경로 설정

                // 오류 리스너 설정: 재생 중 발생할 수 있는 오류 처리
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioPlayer", "MediaPlayer error: what=$what, extra=$extra")
                    mp.release() // 오류 발생 시 MediaPlayer 해제
                    mediaPlayer = null // 참조 제거
                    onCompletionListener?.invoke() // 완료/오류 알림
                    true // 오류를 처리했음을 알림
                }

                prepare() // 파일 준비
                start()
                setOnCompletionListener {
                    onCompletionListener?.invoke() // 재생 완료 시 ViewModel에 알림
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio file: ${e.message}")
            onCompletionListener?.invoke() // 에러 발생 시에도 상태 초기화를 위해 알림
            onCompletionListener?.invoke() // 에러 발생 시에도 상태 초기화를 위해 알림
            //예외 발생 시에도 MediaPlayer를 확실히 해제하고 null로 설정
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        mediaPlayer?.start()
    }

    fun stop() {
        // mediaPlayer가 null이 아닐 때만 stop과 release 호출
        mediaPlayer?.apply {
            // isPlaying 체크는 선택 사항이지만, 이미 stop된 상태에서 다시 stop을 호출하는 것을 방지
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null // 확실히 null로 설정하여 참조 제거
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
}