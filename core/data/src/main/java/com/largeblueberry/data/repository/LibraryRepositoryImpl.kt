package com.largeblueberry.data.repository

import com.largeblueberry.data.audio.AudioRecordDao
import com.largeblueberry.data.mapper.LibraryMapper
import com.largeblueberry.domain.model.LibraryModel
import com.largeblueberry.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LibraryRepositoryImpl @Inject constructor(
    private val audioRecordDao: AudioRecordDao
) : LibraryRepository {

    override fun getAllRecords(): Flow<List<LibraryModel>> {
        return audioRecordDao.getAllRecords().map { entities ->
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
