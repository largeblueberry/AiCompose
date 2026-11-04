package com.largeblueberry.domain.usecase.library

import com.largeblueberry.domain.model.LibraryModel
import com.largeblueberry.domain.repository.LibraryRepository
import java.io.File
import javax.inject.Inject

class DeleteAudioRecordUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(record: LibraryModel): Result<Unit> {
        return try {
            val fileDeleted = File(record.filePath).delete()
            libraryRepository.deleteRecord(record)
            if (!fileDeleted && File(record.filePath).exists()) {
                Result.failure(Exception("파일 삭제 실패"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
