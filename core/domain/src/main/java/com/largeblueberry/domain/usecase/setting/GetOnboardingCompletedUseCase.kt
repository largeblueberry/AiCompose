package com.largeblueberry.domain.usecase.setting

import com.largeblueberry.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOnboardingCompletedUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> = userPreferencesRepository.isOnboardingCompleted
}
