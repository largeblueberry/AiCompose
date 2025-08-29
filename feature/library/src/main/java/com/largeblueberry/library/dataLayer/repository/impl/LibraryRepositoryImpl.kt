package com.largeblueberry.library.dataLayer.repository.impl

import com.largeblueberry.library.domainLayer.repository.LibraryRepository

import com.largeblueberry.library.dataLayer.mapper.LibraryMapper
import com.largeblueberry.library.domainLayer.model.LibraryModel
import com.largeblueberry.local.audio.AudioRecordDao
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