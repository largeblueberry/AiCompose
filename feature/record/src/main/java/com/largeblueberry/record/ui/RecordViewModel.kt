package com.largeblueberry.record.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.domain.model.RecorderState
import com.largeblueberry.domain.model.RecordingConfig
import com.largeblueberry.domain.model.Tempo
import com.largeblueberry.domain.repository.RecordingInterruptedException
import com.largeblueberry.domain.repository.RecordingTooShortException
import com.largeblueberry.domain.usecase.record.CancelRecordingUseCase
import com.largeblueberry.domain.usecase.record.ObserveRecorderStateUseCase
import com.largeblueberry.domain.usecase.record.StartRecordingUseCase
import com.largeblueberry.domain.usecase.record.StopRecordingUseCase
import com.largeblueberry.domain.usecase.setting.GetLastBpmUseCase
import com.largeblueberry.domain.usecase.setting.SetLastBpmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordUiState(
    val bpm: Int = Tempo.DEFAULT_BPM,
    val recorder: RecorderState = RecorderState.Idle,
    val status: RecordingState = RecordingState.WAITING,
    /** 저장된 녹음 파일 이름(스낵바 1회 표시 후 지움) */
    val savedFileName: String? = null,
    val stoppedAtLimit: Boolean = false,
) {
    val isActive: Boolean get() = recorder !is RecorderState.Idle
}

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val cancelRecordingUseCase: CancelRecordingUseCase,
    observeRecorderState: ObserveRecorderStateUseCase,
    private val getLastBpmUseCase: GetLastBpmUseCase,
    private val setLastBpmUseCase: SetLastBpmUseCase,
) : ViewModel() {

    private companion object {
        const val TAG = "RecordViewModel"
        const val MAX_RECORDING_MS = 120_000L
    }

    private val local = MutableStateFlow(RecordUiState())
    private val recorderState: StateFlow<RecorderState> = observeRecorderState()
    private var stopping = false

    /** 화면 구독 여부와 상관없이 녹음기 상태로 판단한다(uiState는 구독이 없으면 멈춘다). */
    private val isRecorderActive: Boolean get() = recorderState.value !is RecorderState.Idle

    val uiState: StateFlow<RecordUiState> = combine(local, recorderState) { l, recorder ->
        val status = when (recorder) {
            is RecorderState.CountIn -> RecordingState.COUNT_IN
            is RecorderState.Recording -> RecordingState.RECORDING
            // 실패는 stopRecording 결과가 나올 때까지 직전 상태를 유지한다
            RecorderState.Failed -> l.status
            RecorderState.Idle -> l.status.takeUnless { it == RecordingState.COUNT_IN || it == RecordingState.RECORDING }
                ?: RecordingState.WAITING
        }
        l.copy(recorder = recorder, status = status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordUiState())

    init {
        viewModelScope.launch {
            val bpm = getLastBpmUseCase().first()
            local.update { it.copy(bpm = bpm) }
        }
        viewModelScope.launch {
            recorderState.collect { s ->
                when {
                    // 최대 길이에 닿으면 자동으로 멈춘다
                    s is RecorderState.Recording && s.elapsedMs >= MAX_RECORDING_MS -> stopRecording(atLimit = true)
                    // 녹음 도중 실패하면 원인을 받아 화면에 알린다(파일은 저장소가 지운다)
                    s is RecorderState.Failed -> stopRecording()
                }
            }
        }
    }

    fun changeBpm(delta: Int) = setBpm(local.value.bpm + delta)

    fun setBpm(bpm: Int) {
        if (isRecorderActive) return
        val v = Tempo.clamp(bpm)
        local.update { it.copy(bpm = v) }
        viewModelScope.launch { setLastBpmUseCase(v) }
    }

    fun startRecording() {
        if (isRecorderActive) return
        val config = RecordingConfig(bpm = local.value.bpm)
        startRecordingUseCase(config)
            .onSuccess {
                Log.i(TAG, "recording started at ${config.bpm} bpm")
                local.update { it.copy(status = RecordingState.COUNT_IN, stoppedAtLimit = false) }
            }
            .onFailure { e ->
                Log.e(TAG, "start failed", e)
                local.update { it.copy(status = RecordingState.FAILED_START) }
            }
    }

    fun stopRecording(atLimit: Boolean = false) {
        if (stopping) return
        stopping = true
        viewModelScope.launch {
            stopRecordingUseCase()
                .onSuccess { record ->
                    Log.i(TAG, "saved ${record.filename} (${record.duration}, ${record.sampleRate}Hz)")
                    local.update {
                        it.copy(status = RecordingState.COMPLETED, savedFileName = record.filename, stoppedAtLimit = atLimit)
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "stop failed", e)
                    val status = when (e) {
                        is RecordingTooShortException -> RecordingState.TOO_SHORT
                        is RecordingInterruptedException -> RecordingState.INTERRUPTED
                        else -> RecordingState.FAILED_STOP
                    }
                    local.update { it.copy(status = status) }
                }
            stopping = false
        }
    }

    /**
     * 화면이 안 보이게 되면(홈 버튼, 다른 앱) 진행 중인 녹음을 버린다.
     * Android 11+는 포그라운드 서비스 없이 백그라운드에서 마이크를 막아 무음만 녹음되기 때문이다.
     */
    fun onScreenStopped() {
        if (!isRecorderActive || stopping) return
        cancelRecordingUseCase()
        local.update { it.copy(status = RecordingState.CANCELLED) }
    }

    fun consumeSavedMessage() = local.update { it.copy(savedFileName = null, stoppedAtLimit = false) }

    override fun onCleared() {
        // 화면을 떠나면 진행 중인 녹음은 버린다(백그라운드 녹음은 지원하지 않음)
        cancelRecordingUseCase()
        super.onCleared()
    }
}
