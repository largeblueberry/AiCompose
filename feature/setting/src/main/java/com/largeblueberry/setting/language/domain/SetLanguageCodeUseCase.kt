package com.largeblueberry.setting.language.domain

import com.largeblueberry.domain.repository.LanguageRepository
import javax.inject.Inject

class SetLanguageCodeUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    suspend operator fun invoke(languageCode: String) {
        languageRepository.setLanguage(languageCode)
    }
}
