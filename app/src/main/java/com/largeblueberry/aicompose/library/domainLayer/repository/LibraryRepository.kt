package com.largeblueberry.aicompose.library.domainLayer.repository

import com.largeblueberry.aicompose.library.domainLayer.model.LibraryModel
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun getAllRecords(): Flow<List<LibraryModel>>

    suspend fun deleteRecord(record: LibraryModel)

    suspend fun renameRecord(record: LibraryModel)
    // 파일 이름 변경 처리
}