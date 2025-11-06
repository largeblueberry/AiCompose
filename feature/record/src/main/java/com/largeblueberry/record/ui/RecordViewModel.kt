package com.largeblueberry.record.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.analyticshelper.AnalyticsHelper
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
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val analyticsHelper: AnalyticsHelper // AnalyticsHelper 주입
) : ViewModel() {

    // Logcat 필터링을 위한 TAG 추가
    private companion object {
        private const val TAG = "RecordViewModel"
    }

    private var currentRecordingFile: File? = null

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _recordingStateText = MutableLiveData(RecordingState.WAITING)
    val recordingStateText: LiveData<RecordingState> = _recordingStateText

    private val _lastSavedFileName = MutableLiveData<String>()
    val lastSavedFileName: LiveData<String> = _lastSavedFileName

    fun startRecording() {
        Log.d(TAG, "startRecording called") // 녹음 시작 시도 로그
        viewModelScope.launch {
            startRecordingUseCase()
                .onSuccess { file ->
                    // 녹음 시작 성공 로그 및 이벤트
                    Log.i(TAG, "Start recording successful")
                    analyticsHelper.logEvent(name = "start_recording", params = emptyMap())

                    currentRecordingFile = file
                    _isRecording.value = true
                    _recordingStateText.value = RecordingState.RECORDING
                }
                .onFailure { e ->
                    // 녹음 시작 실패 로그 및 이벤트
                    Log.e(TAG, "Start recording failed", e)
                    analyticsHelper.logEvent(
                        name = "start_recording_failure",
                        params = mapOf("error_message" to (e.message ?: "Unknown error"))
                    )

                    _recordingStateText.value = RecordingState.FAILED_START
                    _isRecording.value = false
                }
        }
    }

    fun stopRecording() {
        Log.d(TAG, "stopRecording called") // 녹음 중지 시도 로그
        val filePath = currentRecordingFile?.absolutePath ?: run {
            _recordingStateText.value = RecordingState.FAILED_FILE_PATH_NOT_FOUND
            return
        }

        viewModelScope.launch {
            stopRecordingUseCase(filePath)
                .onSuccess { audioRecord ->
                    // 녹음 중지 성공 로그 및 이벤트
                    Log.i(TAG, "Stop recording successful. File: ${audioRecord.filename}")
                    analyticsHelper.logEvent(
                        name = "stop_recording",
                        params = mapOf("file_name" to audioRecord.filename)
                    )

                    _isRecording.value = false
                    _recordingStateText.value = RecordingState.COMPLETED
                    stopRecordingUseCase.saveRecordToDatabase(audioRecord)
                    _lastSavedFileName.postValue(audioRecord.filename)
                }
                .onFailure { e ->
                    // 녹음 중지 실패 로그 및 이벤트
                    Log.e(TAG, "Stop recording failed", e)
                    analyticsHelper.logEvent(
                        name = "stop_recording_failure",
                        params = mapOf("error_message" to (e.message ?: "Unknown error"))
                    )

                    _recordingStateText.value = RecordingState.FAILED_STOP
                }
            currentRecordingFile = null
        }
    }
}