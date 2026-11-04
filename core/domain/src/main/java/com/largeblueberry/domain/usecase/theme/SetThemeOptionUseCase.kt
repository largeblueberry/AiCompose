package com.largeblueberry.domain.usecase.theme

import com.largeblueberry.domain.model.ThemeOption
import com.largeblueberry.domain.repository.ThemeRepository
import javax.inject.Inject

class SetThemeOptionUseCase @Inject constructor(
    private val themeRepository: ThemeRepository
) {
    suspend operator fun invoke(option: ThemeOption) {
        themeRepository.setThemeOption(option)
    }
}
