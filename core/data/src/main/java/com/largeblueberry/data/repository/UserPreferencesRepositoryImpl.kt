package com.largeblueberry.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.largeblueberry.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @Named("user_preferences") private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private const val MAX_RETRIES = 3L
        private val RETRY_DELAY = 1000.milliseconds
    }

    override val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .retryWhen { cause, attempt ->
            if (cause is IOException && attempt < MAX_RETRIES) {
                emit(emptyPreferences())
                delay(RETRY_DELAY)
                true
            } else {
                false
            }
        }
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] ?: false
        }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }
}
