package com.largeblueberry.setting.language.domain

import javax.inject.Inject

class SetLanguageCodeUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    suspend operator fun invoke(languageCode: String) {
        languageRepository.setLanguage(languageCode)
    }
}
