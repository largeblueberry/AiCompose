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
        )// stateflow 를 활용한 개발

    // UI 상태: 데이터가 비어있는지 여부
    private val _isEmpty = MutableStateFlow(true)
    val isEmpty: StateFlow<Boolean> = _isEmpty

    // 삭제 성공/실패 상태 알림용
    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

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
            try {
                val fileDeleted = File(record.filePath).delete()
                audioRecordDao.deleteRecord(record)
                if (!fileDeleted) {
                    // 파일이 이미 없거나 삭제 실패
                    _deleteResult.value = Result.failure(Exception("파일 삭제 실패"))
                } else {
                    _deleteResult.value = Result.success(Unit)
                }
            } catch (e: Exception) {
                _deleteResult.value = Result.failure(e)
            }
        }
    }

    // 공유를 위한 파일 반환 함수
    fun getRecordFile(record: AudioRecordEntity): File {
        return File(record.filePath)
    }

    // 삭제 결과 상태 초기화 (UI에서 메시지 표시 후 호출)
    fun clearDeleteResult() {
        _deleteResult.value = null
    }
}