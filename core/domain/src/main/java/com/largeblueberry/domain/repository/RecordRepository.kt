package com.largeblueberry.domain.repository

import com.largeblueberry.domain.model.RecordModel
import java.io.File

interface RecordRepository {
    fun startRecording(): Result<File>
    fun stopRecording(): Result<Long>
    fun release()
    suspend fun saveRecord(recordModel: RecordModel)
}
