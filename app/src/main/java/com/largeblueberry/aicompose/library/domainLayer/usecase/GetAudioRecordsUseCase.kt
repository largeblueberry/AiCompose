package com.largeblueberry.aicompose.library.domainLayer.usecase

import com.largeblueberry.aicompose.dataLayer.model.local.AudioRecordEntity
import com.largeblueberry.aicompose.library.dataLayer.repository.AudioRecordRepository
import kotlinx.coroutines.flow.Flow

class GetAudioRecordsUseCase(private val repository: AudioRecordRepository) {
    operator fun invoke(): Flow<List<AudioRecordEntity>> {
        return repository.getAllRecords()
    }
}