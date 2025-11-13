package com.largeblueberry.setting.language.data

import android.util.Log // 1. Log 임포트
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.largeblueberry.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class LanguageRepositoryImpl @Inject constructor(
    @Named("language") private val dataStore: DataStore<Preferences>
) : LanguageRepository {

    // 2. Logcat 필터링을 위한 TAG 추가
    private companion object {
        private const val TAG = "LanguageRepositoryImpl"
    }

    private object PreferencesKeys {
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    override val language: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] ?: Locale.getDefault().language
        }

    override suspend fun setLanguage(languageCode: String) {
        // 3. setLanguage 메서드가 호출될 때 로그 출력
        Log.d(TAG, "setLanguage called with code: $languageCode")

        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = languageCode
        }
    }
}
