package com.largeblueberry.aicompose.record.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.largeblueberry.aicompose.record.ui.screen.RecordScreenState

class RecordActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecordScreenState()
        }
    }
}