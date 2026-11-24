package com.largeblueberry.feature_sheetmusic.ui.util

import android.media.MediaPlayer
import android.util.Log

/** 악보의 MIDI 파일을 기기 내장 신시사이저로 재생한다(MediaPlayer가 .mid를 지원한다). */
class MidiPlayer {
    private var player: MediaPlayer? = null

    val isPlaying: Boolean get() = player?.isPlaying == true

    /** @return 시작했으면 true */
    fun play(path: String, onComplete: () -> Unit): Boolean {
        stop()
        return try {
            player = MediaPlayer().apply {
                setDataSource(path)
                setOnCompletionListener {
                    stop()
                    onComplete()
                }
                prepare()
                start()
            }
            true
        } catch (e: Exception) {
            Log.e("MidiPlayer", "play failed: $path", e)
            stop()
            false
        }
    }

    fun stop() {
        player?.run {
            try {
                if (isPlaying) stop()
            } catch (_: IllegalStateException) {
            }
            release()
        }
        player = null
    }
}
