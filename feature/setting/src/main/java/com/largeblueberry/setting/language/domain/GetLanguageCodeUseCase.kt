package com.largeblueberry.setting.language.domain

import com.largeblueberry.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLanguageCodeUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    operator fun invoke(): Flow<String> = languageRepository.language
}
