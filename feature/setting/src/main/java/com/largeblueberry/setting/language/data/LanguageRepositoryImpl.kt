package com.largeblueberry.setting.language.data

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.largeblueberry.domain.repository.LanguageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor() : LanguageRepository {

    private companion object {
        private const val TAG = "LanguageRepositoryImpl"
    }

    override val language: Flow<String> = flow {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val languageCode = if (!appLocales.isEmpty) {
            appLocales[0]?.language ?: Locale.getDefault().language
        } else {
            Locale.getDefault().language
        }
        emit(languageCode)
    }

    override suspend fun setLanguage(languageCode: String) {
        Log.d(TAG, "setLanguage called with code: $languageCode")

        withContext(Dispatchers.Main) {
            val appLocale = if (languageCode.isBlank()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(languageCode)
            }
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}