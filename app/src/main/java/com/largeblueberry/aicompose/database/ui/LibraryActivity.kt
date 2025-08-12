package com.largeblueberry.aicompose.database.ui

import android.content.Intent
import android.os.Bundle // Bundle 임포트 추가
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.largeblueberry.aicompose.database.ui.viemodel.LibraryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt가 이 액티비티에 의존성을 주입할 수 있도록 설정
class LibraryActivity : ComponentActivity() {

    private val viewModel: LibraryViewModel by viewModels()
    private val audioPlayer = AudioPlayer()

    override fun onCreate(savedInstanceState: Bundle?) { // savedInstanceState 타입 명시
        super.onCreate(savedInstanceState)
        setContent {
            LibraryScreen(
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
        startActivity(Intent.createChooser(sendIntent, "업로드 하기"))
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
    }
}