package com.largeblueberry.domain.repository

import com.largeblueberry.domain.model.LibraryModel
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun getAllRecords(): Flow<List<LibraryModel>>
    suspend fun deleteRecord(record: LibraryModel)
    suspend fun renameRecord(record: LibraryModel)
}
