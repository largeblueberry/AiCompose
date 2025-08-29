package com.largeblueberry.library.domainLayer.usecase

import com.largeblueberry.library.domainLayer.model.LibraryModel // LibraryAudioRecord 임포트
import com.largeblueberry.library.domainLayer.repository.LibraryRepository
import java.io.File

class RenameAudioRecordUseCase(private val repository: LibraryRepository) {

    suspend operator fun invoke(record: LibraryModel, newName: String): Result<Unit> {
        return try {
            val oldFile = File(record.filePath)
            val parentDir = oldFile.parentFile

            // 파일이 존재하지 않거나, 부모 디렉토리를 찾을 수 없는 경우 예외 처리
            if (!oldFile.exists()) {
                return Result.failure(Exception("원본 파일이 존재하지 않습니다: ${record.filePath}"))
            }
            if (parentDir == null) {
                return Result.failure(Exception("파일의 부모 디렉토리를 찾을 수 없습니다: ${record.filePath}"))
            }

            val fileExtension = oldFile.extension // 확장자 유지
            val newFileName = if (fileExtension.isNotEmpty()) "$newName.$fileExtension" else newName
            val newFile = File(parentDir, newFileName)

            // 실제 파일 이름 변경
            val renamed = oldFile.renameTo(newFile)
            if (renamed) {
                // DB의 파일명, 경로 업데이트
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
