package com.largeblueberry.aicompose.database.UI

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AudioRecordViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioRecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioRecordViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}