package com.largeblueberry.library.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

class AudioPlayer(context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null

    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    fun play(filePath: String) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioPlayer", "MediaPlayer error: what=$what, extra=$extra")
                    mp.release()
                    mediaPlayer = null
                    onCompletionListener?.invoke()
                    true
                }
                prepare()
                start()
                setOnCompletionListener {
                    onCompletionListener?.invoke()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio file: ${e.message}")
            onCompletionListener?.invoke()
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
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
}