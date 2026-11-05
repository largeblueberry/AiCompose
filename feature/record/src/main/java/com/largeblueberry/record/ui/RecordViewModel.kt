package com.largeblueberry.record.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.domain.usecase.record.StartRecordingUseCase
import com.largeblueberry.domain.usecase.record.StopRecordingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "RecordViewModel"
    }

    private var currentRecordingFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingStateText = MutableStateFlow(RecordingState.WAITING)
    val recordingStateText: StateFlow<RecordingState> = _recordingStateText.asStateFlow()

    private val _lastSavedFileName = MutableStateFlow<String?>(null)
    val lastSavedFileName: StateFlow<String?> = _lastSavedFileName.asStateFlow()

    fun startRecording() {
        Log.d(TAG, "startRecording called")
        viewModelScope.launch {
            startRecordingUseCase()
                .onSuccess { file ->
                    Log.i(TAG, "Start recording successful")
                    currentRecordingFile = file
                    _isRecording.value = true
                    _recordingStateText.value = RecordingState.RECORDING
                }
                .onFailure { e ->
                    Log.e(TAG, "Start recording failed", e)
                    _recordingStateText.value = RecordingState.FAILED_START
                    _isRecording.value = false
                }
        }
    }

    fun stopRecording() {
        Log.d(TAG, "stopRecording called")
        val filePath = currentRecordingFile?.absolutePath ?: run {
            _recordingStateText.value = RecordingState.FAILED_FILE_PATH_NOT_FOUND
            return
        }

        viewModelScope.launch {
            stopRecordingUseCase(filePath)
                .onSuccess { audioRecord ->
                    Log.i(TAG, "Stop recording successful. File: ${audioRecord.filename}")
                    _isRecording.value = false
                    _recordingStateText.value = RecordingState.COMPLETED
                    stopRecordingUseCase.saveRecordToDatabase(audioRecord)
                    _lastSavedFileName.value = audioRecord.filename
                }
                .onFailure { e ->
                    Log.e(TAG, "Stop recording failed", e)
                    _recordingStateText.value = RecordingState.FAILED_STOP
                }
            currentRecordingFile = null
        }
    }
}