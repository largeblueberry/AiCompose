package com.largeblueberry.aicompose.library.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.largeblueberry.aicompose.library.ui.screen.LibraryScreen
import com.largeblueberry.aicompose.library.ui.viemodel.LibraryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryActivity : ComponentActivity() {

    private val viewModel: LibraryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LibraryScreen(
                viewModel = viewModel,
                onUploadSuccess = { url -> shareUrl(url) }
            )
        }
    }

    private fun shareUrl(url: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND // 'this.action =' 또는 'action ='으로 변경
            putExtra(Intent.EXTRA_TEXT, url) // putExtra 호출 방식 수정
            type = "text/plain" // 'this.type =' 또는 'type ='으로 변경
        }
        startActivity(Intent.createChooser(sendIntent, "업로드 하기"))
    }
}