package com.largeblueberry.domain.usecase.library

import com.largeblueberry.domain.model.LibraryModel
import com.largeblueberry.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAudioRecordsUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    operator fun invoke(): Flow<List<LibraryModel>> {
        return repository.getAllRecords()
    }
}
