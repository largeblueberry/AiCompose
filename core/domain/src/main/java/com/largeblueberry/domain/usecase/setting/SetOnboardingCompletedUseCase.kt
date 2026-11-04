package com.largeblueberry.domain.usecase.setting

import com.largeblueberry.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetOnboardingCompletedUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(completed: Boolean) {
        userPreferencesRepository.setOnboardingCompleted(completed)
    }
}
