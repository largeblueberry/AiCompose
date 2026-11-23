package com.largeblueberry.domain.repository

import com.largeblueberry.domain.model.RecordModel
import com.largeblueberry.domain.model.RecorderState
import com.largeblueberry.domain.model.RecordingConfig
import kotlinx.coroutines.flow.StateFlow

interface RecordRepository {
    val state: StateFlow<RecorderState>

    /** 카운트인 → 녹음. 이미 녹음 중이면 실패. */
    fun startRecording(config: RecordingConfig): Result<Unit>

    /** 녹음을 끝내고 파일 정보를 돌려준다. 카운트인 중에 멈추면(녹음된 소리 없음) 실패하고 파일을 지운다. */
    fun stopRecording(): Result<RecordModel>

    /** 녹음을 버린다. */
    fun cancelRecording()

    suspend fun saveRecord(recordModel: RecordModel)
}

/** 카운트인 중에 멈춰서 녹음된 소리가 없다. */
class RecordingTooShortException : Exception("nothing recorded after count-in")

/** 전화 등 다른 앱이 오디오를 가져가서 녹음이 중단됐다. */
class RecordingInterruptedException : Exception("recording interrupted by audio focus loss")
