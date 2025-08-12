package com.largeblueberry.aicompose.library.dataLayer.repository

import com.largeblueberry.aicompose.dataLayer.model.local.AudioRecordEntity

interface AudioRecordRepository {
    fun getAllRecords(): kotlinx.coroutines.flow.Flow<List<AudioRecordEntity>>

    suspend fun deleteRecord(record: AudioRecordEntity)

    suspend fun updateRecord(record: AudioRecordEntity)
    // 파일 이름 변경 처리
}