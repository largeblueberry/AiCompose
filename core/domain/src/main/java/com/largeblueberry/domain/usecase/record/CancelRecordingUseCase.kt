package com.largeblueberry.domain.usecase.record

import com.largeblueberry.domain.repository.RecordRepository
import javax.inject.Inject

class CancelRecordingUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    operator fun invoke() = recordRepository.cancelRecording()
}
