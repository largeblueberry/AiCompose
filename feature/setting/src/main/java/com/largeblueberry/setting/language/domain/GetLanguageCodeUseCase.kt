package com.largeblueberry.setting.language.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLanguageCodeUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    operator fun invoke(): Flow<String> = languageRepository.language
}
