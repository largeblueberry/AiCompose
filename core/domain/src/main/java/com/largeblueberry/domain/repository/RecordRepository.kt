package com.largeblueberry.domain.repository

import com.largeblueberry.domain.model.RecordModel
import com.largeblueberry.domain.model.RecorderState
import com.largeblueberry.domain.model.RecordingConfig
import kotlinx.coroutines.flow.StateFlow

/**
 * 녹음기. 구현은 main-safe해야 한다: 블로킹 작업(스레드 정리, 파일 쓰기)은 구현이 알아서 다른 스레드로 넘긴다.
 * 그래서 호출하는 쪽(유스케이스·ViewModel)은 스레드를 고르지 않는다.
 */
interface RecordRepository {
    val state: StateFlow<RecorderState>

    /** 카운트인 → 녹음. 이미 녹음 중이면 실패. */
    fun startRecording(config: RecordingConfig): Result<Unit>

    /** 녹음을 끝내고 파일 정보를 돌려준다. 카운트인 중에 멈추면(녹음된 소리 없음) 실패하고 파일을 지운다. */
    suspend fun stopRecording(): Result<RecordModel>

    /**
     * 녹음을 버린다. 바로 반환하고 정리(스레드 종료, 파일 삭제)는 뒤에서 한다.
     * suspend가 아닌 이유: ViewModel.onCleared처럼 코루틴을 쓸 수 없는 곳에서도 부른다.
     */
    fun cancelRecording()

    suspend fun saveRecord(recordModel: RecordModel)
}

/** 카운트인 중에 멈춰서 녹음된 소리가 없다. */
class RecordingTooShortException : Exception("nothing recorded after count-in")

/** 전화 등 다른 앱이 오디오를 가져가서 녹음이 중단됐다. */
class RecordingInterruptedException : Exception("recording interrupted by audio focus loss")
