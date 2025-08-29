package com.largeblueberry.record.domain.repository

import com.largeblueberry.record.domain.model.RecordModel
import java.io.File


interface RecordRepository {

    // 녹음 시작 시 출력 파일 경로를 반환
    fun startRecording(): Result<File>

    // 녹음 중지 시 녹음된 파일의 길이를 반환
    fun stopRecording(): Result<Long>

    // 녹음이 진행 중인지 확인
    fun release()

    suspend fun saveRecord(recordModel: RecordModel)
}