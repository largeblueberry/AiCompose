package com.largeblueberry.aicompose.library.dataLayer.repository.impl

import com.largeblueberry.aicompose.library.dataLayer.repository.AudioRecordRepository
import com.largeblueberry.aicompose.dataLayer.repository.AudioRecordDao
import com.largeblueberry.aicompose.dataLayer.model.local.AudioRecordEntity

class AudioRecordRepositoryImpl(
    private val audioRecordDao: AudioRecordDao
) : AudioRecordRepository {

    override fun getAllRecords() = audioRecordDao.getAllRecords()

    override suspend fun deleteRecord(record: AudioRecordEntity) {
        audioRecordDao.deleteRecord(record)
    }

    override suspend fun updateRecord(record:AudioRecordEntity) {
        audioRecordDao.updateRecord(record)
        // 이게 이름 바꾸는 거임.
    }
}