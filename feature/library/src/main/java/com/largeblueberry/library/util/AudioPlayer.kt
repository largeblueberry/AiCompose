package com.largeblueberry.library.util

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null

    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun play(filePath: String) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            mediaPlayer = MediaPlayer(context).apply {
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