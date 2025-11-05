package com.largeblueberry.aicompose.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.largeblueberry.setting.language.domain.LanguageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    private val languageRepository: LanguageRepository
) {

    init {
        // 앱 시작 시 저장된 언어를 AppCompatDelegate에 적용
        CoroutineScope(Dispatchers.Main).launch {
            languageRepository.language.collect { languageCode ->
                applyLanguageToSystem(languageCode)
            }
        }
    }

    private fun applyLanguageToSystem(languageCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        if (AppCompatDelegate.getApplicationLocales() != appLocale) {
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}
