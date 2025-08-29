package com.largeblueberry.library.domainLayer.usecase

import com.largeblueberry.library.domainLayer.model.LibraryModel
import com.largeblueberry.library.domainLayer.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow

class GetAudioRecordsUseCase(private val repository: LibraryRepository) {
    operator fun invoke(): Flow<List<LibraryModel>> {
        return repository.getAllRecords()
    }
}