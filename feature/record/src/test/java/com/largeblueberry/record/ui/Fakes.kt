package com.largeblueberry.record.ui

import com.largeblueberry.domain.model.RecordModel
import com.largeblueberry.domain.model.RecorderState
import com.largeblueberry.domain.model.RecordingConfig
import com.largeblueberry.domain.model.Tempo
import com.largeblueberry.domain.repository.RecordRepository
import com.largeblueberry.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 녹음기 가짜. 상태는 테스트가 직접 움직인다(실제 녹음 스레드 대신). */
class FakeRecordRepository : RecordRepository {
    val mutableState = MutableStateFlow<RecorderState>(RecorderState.Idle)
    override val state: StateFlow<RecorderState> = mutableState

    var startResult: Result<Unit> = Result.success(Unit)
    var stopResult: Result<RecordModel> = Result.success(record())
    val startedConfigs = mutableListOf<RecordingConfig>()
    val savedRecords = mutableListOf<RecordModel>()
    var stopCalls = 0
    var cancelCalls = 0

    override fun startRecording(config: RecordingConfig): Result<Unit> {
        startedConfigs += config
        if (startResult.isSuccess) {
            mutableState.value = RecorderState.CountIn(config.countInBeats, 0, clickAudible = false)
        }
        return startResult
    }

    override suspend fun stopRecording(): Result<RecordModel> {
        stopCalls++
        mutableState.value = RecorderState.Idle
        return stopResult
    }

    override fun cancelRecording() {
        cancelCalls++
        mutableState.value = RecorderState.Idle
    }

    override suspend fun saveRecord(recordModel: RecordModel) {
        savedRecords += recordModel
    }

    companion object {
        fun record(name: String = "20261123_120000.wav", bpm: Int = 100) = RecordModel(
            filename = name,
            filePath = "/data/recordings/$name",
            fileSize = 1_000,
            duration = "00:05",
            bpm = bpm,
            sampleRate = 22_050,
        )
    }
}

class FakeUserPreferencesRepository(initialBpm: Int = Tempo.DEFAULT_BPM) : UserPreferencesRepository {
    val bpm = MutableStateFlow(initialBpm)
    override val isOnboardingCompleted: Flow<Boolean> = MutableStateFlow(true)
    override suspend fun setOnboardingCompleted(completed: Boolean) = Unit
    override val lastBpm: Flow<Int> = bpm
    override suspend fun setLastBpm(bpm: Int) {
        this.bpm.value = bpm
    }
}
