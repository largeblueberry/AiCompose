package com.largeblueberry.domain.usecase.record

import com.largeblueberry.domain.model.RecorderState
import com.largeblueberry.domain.repository.RecordRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveRecorderStateUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    operator fun invoke(): StateFlow<RecorderState> = recordRepository.state
}
