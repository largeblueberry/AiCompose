package com.largeblueberry.domain.usecase.record

import com.largeblueberry.domain.repository.RecordRepository
import java.io.File
import javax.inject.Inject

class StartRecordingUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    operator fun invoke(): Result<File> {
        return recordRepository.startRecording()
    }
}
