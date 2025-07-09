package com.largeblueberry.aicompose.database.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.largeblueberry.aicompose.database.ui.viemodel.AudioRecordViewModel
import com.largeblueberry.aicompose.database.ui.viemodel.AudioRecordViewModelFactory
import com.largeblueberry.aicompose.record.ui.AudioPlayer

class AudioRecordDataActivity : ComponentActivity() {

    private val viewModel: AudioRecordViewModel by viewModels {
        AudioRecordViewModelFactory(applicationContext)
    }
    private val audioPlayer = AudioPlayer()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioRecordScreen(
                viewModel = viewModel,
                audioPlayer = audioPlayer,
                onShare = { url -> shareUrl(url) }
            )
        }
    }

    private fun shareUrl(url: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "공유하기"))
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
    }
}