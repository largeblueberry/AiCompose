package com.largeblueberry.record.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.record.domain.usecase.StartRecordingUseCase
import com.largeblueberry.record.domain.usecase.StopRecordingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import com.largeblueberry.record.ui.RecordingState

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase
) : ViewModel() {

    private var currentRecordingFile: File? = null

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _recordingStateText = MutableLiveData(RecordingState.WAITING)
    val recordingStateText: LiveData<RecordingState> = _recordingStateText

    private val _lastSavedFileName = MutableLiveData<String>()
    val lastSavedFileName: LiveData<String> = _lastSavedFileName

    fun startRecording() {
        viewModelScope.launch {
            startRecordingUseCase()
                .onSuccess { file ->
                    currentRecordingFile = file
                    _isRecording.value = true
                    _recordingStateText.value = RecordingState.RECORDING
                }
                .onFailure { e ->
                    _recordingStateText.value = RecordingState.FAILED_START
                    _isRecording.value = false
                }
        }
    }
    
    fun stopRecording() {
        val filePath = currentRecordingFile?.absolutePath ?: run {
            _recordingStateText.value = RecordingState.FAILED_FILE_PATH_NOT_FOUND
            return
        }

        viewModelScope.launch {
            stopRecordingUseCase(filePath)
                .onSuccess { audioRecord ->
                    _isRecording.value = false
                    _recordingStateText.value = RecordingState.COMPLETED
                    stopRecordingUseCase.saveRecordToDatabase(audioRecord)
                    _lastSavedFileName.postValue(audioRecord.filename)
                }
                .onFailure { e ->
                    _recordingStateText.value = RecordingState.FAILED_STOP
                }
            currentRecordingFile = null
        }
    }
}