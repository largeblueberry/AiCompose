package com.largeblueberry.domain.usecase.setting

import com.largeblueberry.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLastBpmUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Int> = repository.lastBpm
}
