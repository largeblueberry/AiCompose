package com.largeblueberry.domain.usecase.setting

import com.largeblueberry.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetLastBpmUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(bpm: Int) = repository.setLastBpm(bpm)
}
