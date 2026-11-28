package com.largeblueberry.data.repository

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.largeblueberry.domain.repository.LanguageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

/**
 * 앱 언어. 언어를 바꾸면 Activity는 재생성되지만 ViewModel은 남으므로, 값을 한 번만 내는 flow로는
 * 화면이 바뀐 언어를 받지 못한다. 그래서 바꿀 때마다 새 값을 내는 StateFlow로 둔다(@Singleton 바인딩).
 */
class LanguageRepositoryImpl @Inject constructor() : LanguageRepository {

    private companion object {
        private const val TAG = "LanguageRepositoryImpl"
    }

    private val current = MutableStateFlow(readLanguageCode())

    override val language: Flow<String> = current.asStateFlow()

    override suspend fun setLanguage(languageCode: String) {
        Log.d(TAG, "setLanguage called with code: $languageCode")

        withContext(Dispatchers.Main) {
            val appLocale = if (languageCode.isBlank()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(languageCode)
            }
            AppCompatDelegate.setApplicationLocales(appLocale)
            current.value = readLanguageCode()
        }
    }

    private fun readLanguageCode(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        return if (!appLocales.isEmpty) {
            appLocales[0]?.language ?: Locale.getDefault().language
        } else {
            Locale.getDefault().language
        }
    }
}
