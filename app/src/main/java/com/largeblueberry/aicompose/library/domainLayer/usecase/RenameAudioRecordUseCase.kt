package com.largeblueberry.aicompose.library.domainLayer.usecase

import com.largeblueberry.aicompose.dataLayer.model.local.AudioRecordEntity
import com.largeblueberry.aicompose.library.dataLayer.repository.AudioRecordRepository
import java.io.File

class RenameAudioRecordUseCase(private val repository: AudioRecordRepository) {
    suspend operator fun invoke(record: AudioRecordEntity, newName: String): Result<Unit> {
        return try{
            val oldFile = File(record.filePath)
            val parentDir = oldFile.parentFile
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
                repository.updateRecord(updatedRecord)
                Result.success(Unit)
            } else {
                // 파일 이름 변경 실패
                Result.failure(Exception("파일 이름 변경 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}