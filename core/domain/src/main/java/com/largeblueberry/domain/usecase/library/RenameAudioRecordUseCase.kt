package com.largeblueberry.domain.usecase.library

import com.largeblueberry.domain.model.LibraryModel
import com.largeblueberry.domain.repository.LibraryRepository
import java.io.File
import javax.inject.Inject

class RenameAudioRecordUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(record: LibraryModel, newName: String): Result<Unit> {
        return try {
            val oldFile = File(record.filePath)
            val parentDir = oldFile.parentFile

            if (!oldFile.exists()) {
                return Result.failure(Exception("원본 파일이 존재하지 않습니다: ${record.filePath}"))
            }
            if (parentDir == null) {
                return Result.failure(Exception("파일의 부모 디렉토리를 찾을 수 없습니다: ${record.filePath}"))
            }

            val fileExtension = oldFile.extension
            val newFileName = if (fileExtension.isNotEmpty()) "$newName.$fileExtension" else newName
            val newFile = File(parentDir, newFileName)

            val renamed = oldFile.renameTo(newFile)
            if (renamed) {
                val updatedRecord = record.copy(
                    filename = newFileName,
                    filePath = newFile.absolutePath
                )
                repository.renameRecord(updatedRecord)
                Result.success(Unit)
            } else {
                Result.failure(Exception("파일 이름 변경 실패: ${oldFile.name} -> ${newFile.name}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
