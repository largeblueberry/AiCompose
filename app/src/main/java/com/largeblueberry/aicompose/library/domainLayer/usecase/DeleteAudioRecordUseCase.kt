package com.largeblueberry.aicompose.library.domainLayer.usecase

import com.largeblueberry.aicompose.library.domainLayer.model.LibraryModel
import com.largeblueberry.aicompose.library.domainLayer.repository.LibraryRepository
import java.io.File

class DeleteAudioRecordUseCase(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(record: LibraryModel): Result<Unit> {
        return try {
            val fileDeleted = File(record.filePath).delete()
            libraryRepository.deleteRecord(record)
            if (!fileDeleted && File(record.filePath).exists()) {
                // 파일 삭제 실패하고, 파일이 여전히 존재하는 경우
                Result.failure(Exception("파일 삭제 실패"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}