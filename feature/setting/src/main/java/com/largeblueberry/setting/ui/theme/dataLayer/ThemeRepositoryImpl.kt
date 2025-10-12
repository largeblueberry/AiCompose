package com.largeblueberry.setting.ui.theme.dataLayer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.largeblueberry.setting.ui.theme.domain.ThemeOption
import com.largeblueberry.setting.ui.theme.domain.ThemeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ThemeRepository {

    private val themeKey = stringPreferencesKey("theme_option")

    override fun getThemeOption(): Flow<ThemeOption> {
        return context.dataStore.data.map { preferences ->
            val themeString = preferences[themeKey] ?: ThemeOption.SYSTEM.key
            when(themeString) {
                ThemeOption.SYSTEM.key -> ThemeOption.SYSTEM
                ThemeOption.LIGHT.key -> ThemeOption.LIGHT
                ThemeOption.DARK.key -> ThemeOption.DARK
                else -> ThemeOption.SYSTEM // 기본값
            }
        }
    }

    override suspend fun setThemeOption(option: ThemeOption) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = option.key // name -> key로 변경
        }
    }
}
