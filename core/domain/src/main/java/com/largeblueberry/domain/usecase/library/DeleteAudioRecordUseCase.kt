package com.largeblueberry.domain.usecase.library

import com.largeblueberry.domain.model.LibraryModel
import com.largeblueberry.domain.repository.LibraryRepository
import com.largeblueberry.domain.repository.TranscriptionScheduler
import java.io.File
import javax.inject.Inject

/**
 * 녹음 파일과 행을 지운다. 그 녹음의 채보 작업이 대기·진행 중이면 먼저 멈춘다.
 * 이미 시작한 채보가 끝까지 가더라도 저장소가 녹음이 없어진 것을 알아채 RECORD_GONE으로 끝낸다
 * (docs/troubleshooting.md B-18 결정 7).
 */
class DeleteAudioRecordUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val transcriptionScheduler: TranscriptionScheduler,
) {
    suspend operator fun invoke(record: LibraryModel): Result<Unit> {
        return try {
            transcriptionScheduler.cancel(record.id)
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
