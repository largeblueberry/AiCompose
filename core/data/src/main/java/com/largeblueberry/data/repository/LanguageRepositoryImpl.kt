package com.largeblueberry.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.largeblueberry.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : LanguageRepository {

    private object PreferencesKeys {
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    override fun getLanguageCode(): Flow<String> {
        return dataStore.data.map {
            it[PreferencesKeys.LANGUAGE_CODE] ?: "ko"
        }
    }

    override suspend fun setLanguageCode(languageCode: String) {
        dataStore.edit {
            it[PreferencesKeys.LANGUAGE_CODE] = languageCode
        }
    }
}
