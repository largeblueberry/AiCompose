package com.largeblueberry.record.domain.usecase

import com.largeblueberry.record.domain.repository.RecordRepository
import com.largeblueberry.record.domain.model.RecordModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class StopRecordingUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
) {
    operator fun invoke(filePath: String): Result<RecordModel> {
        return recordRepository.stopRecording().map { durationMillis ->
            createAudioRecord(filePath, durationMillis)
        }
    }

    private fun createAudioRecord(filePath: String, durationMillis: Long): RecordModel {
        val fileName = filePath.substring(filePath.lastIndexOf("/") + 1)
        val createdAt = System.currentTimeMillis()
        val durationFormatted = formatRecordingDuration(durationMillis.toInt())
        return RecordModel(
            filename = fileName,
            filePath = filePath,
            fileSize = File(filePath).length(),
            createdAt = createdAt,
            duration = durationFormatted
        )
    }

    private fun formatRecordingDuration(durationMillis: Int): String {
        val minutes = (durationMillis / 1000) / 60
        val seconds = (durationMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    suspend fun saveRecordToDatabase(recordModel: RecordModel) {
        withContext(Dispatchers.IO) {
            recordRepository.saveRecord(recordModel)
        }
    }
}
