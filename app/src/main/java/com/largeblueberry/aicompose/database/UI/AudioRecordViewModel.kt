package com.largeblueberry.aicompose.database.UI

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.aicompose.record.database.AudioDatabase
import com.largeblueberry.aicompose.record.database.AudioRecordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class AudioRecordViewModel(context: Context) : ViewModel() {

    private val audioRecordDao = AudioDatabase.getDatabase(context).audioRecordDao()

    // 녹음 기록 리스트
    val audioRecords: StateFlow<List<AudioRecordEntity>> = audioRecordDao.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI 상태: 데이터가 비어있는지 여부
    private val _isEmpty = MutableStateFlow(true)
    val isEmpty: StateFlow<Boolean> = _isEmpty

    init {
        // 데이터가 비어있는지 여부를 업데이트
        viewModelScope.launch {
            audioRecords.collect { records ->
                _isEmpty.value = records.isEmpty()
            }
        }
    }

    fun deleteRecord(record: AudioRecordEntity) {
        viewModelScope.launch {
            val fileDeleted = File(record.filePath).delete()
            audioRecordDao.deleteRecord(record)
        }
    }
}