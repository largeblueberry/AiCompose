package com.largeblueberry.aicompose.library.dataLayer.repository.impl

import com.largeblueberry.aicompose.library.domainLayer.repository.LibraryRepository
import com.largeblueberry.aicompose.data.record.local.AudioRecordDao
import com.largeblueberry.aicompose.library.dataLayer.mapper.LibraryMapper
import com.largeblueberry.aicompose.library.domainLayer.model.LibraryModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryRepositoryImpl(
    private val audioRecordDao: AudioRecordDao
) : LibraryRepository {

    override fun getAllRecords(): Flow<List<LibraryModel>>{
        return audioRecordDao.getAllRecords().map{ entities ->
            entities.map { entity -> LibraryMapper.toDomain(entity) }
        }
    }

    override suspend fun deleteRecord(record: LibraryModel) {

        audioRecordDao.deleteRecord(LibraryMapper.toEntity(record))
    }

    override suspend fun renameRecord(record: LibraryModel) {
        audioRecordDao.renameRecord(LibraryMapper.toEntity(record))

    }
}