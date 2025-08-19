package com.largeblueberry.aicompose.record.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.aicompose.record.domain.usecase.StartRecordingUseCase
import com.largeblueberry.aicompose.record.domain.usecase.StopRecordingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase
) : ViewModel() {

    private var currentRecordingFile: File? = null

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _recordingStateText = MutableLiveData("대기 중")
    val recordingStateText: LiveData<String> = _recordingStateText

    private val _lastSavedFileName = MutableLiveData<String>()
    val lastSavedFileName: LiveData<String> = _lastSavedFileName

    fun startRecording() {
        viewModelScope.launch {
            startRecordingUseCase()
                .onSuccess { file ->
                    currentRecordingFile = file
                    _isRecording.value = true
                    _recordingStateText.value = "녹음 중..."
                }
                .onFailure { e ->
                    _recordingStateText.value = "녹음 시작 실패: ${e.message}"
                    _isRecording.value = false
                }
        }
    }
    
    fun stopRecording() {
        val filePath = currentRecordingFile?.absolutePath ?: run {
            _recordingStateText.value = "녹음 중지 실패: 파일 경로를 찾을 수 없습니다."
            return
        }

        viewModelScope.launch {
            stopRecordingUseCase(filePath)
                .onSuccess { audioRecord ->
                    _isRecording.value = false
                    _recordingStateText.value = "녹음 완료"
                    stopRecordingUseCase.saveRecordToDatabase(audioRecord)
                    _lastSavedFileName.postValue(audioRecord.filename)
                }
                .onFailure { e ->
                    _recordingStateText.value = "녹음 중지 실패: ${e.message}"
                }
            currentRecordingFile = null
        }
    }
}