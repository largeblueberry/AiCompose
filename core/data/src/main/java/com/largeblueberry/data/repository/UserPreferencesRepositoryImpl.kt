package com.largeblueberry.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.largeblueberry.domain.model.Tempo
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
        val KEY_LAST_BPM = intPreferencesKey("last_bpm")
        private const val MAX_RETRIES = 3L
        private val RETRY_DELAY = 1000.milliseconds
    }

    /** IOException이면 몇 번 다시 읽고, 그래도 안 되면 빈 값으로 계속한다(스트림이 끊기지 않게). */
    private val preferences: Flow<Preferences> = dataStore.data
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

    override val isOnboardingCompleted: Flow<Boolean> = preferences.map { it[KEY_ONBOARDING_COMPLETED] ?: false }

    override val lastBpm: Flow<Int> = preferences.map { prefs ->
        Tempo.clamp(prefs[KEY_LAST_BPM] ?: Tempo.DEFAULT_BPM)
    }

    override suspend fun setLastBpm(bpm: Int) {
        dataStore.edit { it[KEY_LAST_BPM] = Tempo.clamp(bpm) }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }
}
