package com.largeblueberry.setting.language.data

import com.largeblueberry.setting.language.domain.LanguageRepository
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.get

class LanguageRepositoryImpl @Inject constructor(
    @Named("language")private val dataStore: DataStore<Preferences>
) : LanguageRepository {

    private object PreferencesKeys {
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    override val language: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] ?: Locale.getDefault().language
        }

    override suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = languageCode
        }
    }
}