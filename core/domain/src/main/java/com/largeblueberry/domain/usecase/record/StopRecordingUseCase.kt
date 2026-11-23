package com.largeblueberry.domain.usecase.record

import com.largeblueberry.domain.model.RecordModel
import com.largeblueberry.domain.repository.RecordRepository
import javax.inject.Inject

/** 녹음을 끝내고 라이브러리(DB)에 저장한다. */
class StopRecordingUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
) {
    suspend operator fun invoke(): Result<RecordModel> =
        recordRepository.stopRecording().mapCatching { record ->
            recordRepository.saveRecord(record)
            record
        }
}
