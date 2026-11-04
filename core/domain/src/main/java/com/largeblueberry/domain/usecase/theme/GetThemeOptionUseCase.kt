package com.largeblueberry.domain.usecase.theme

import com.largeblueberry.domain.model.ThemeOption
import com.largeblueberry.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThemeOptionUseCase @Inject constructor(
    private val themeRepository: ThemeRepository
) {
    operator fun invoke(): Flow<ThemeOption> = themeRepository.getThemeOption()
}
