package com.largeblueberry.setting.ui.theme.dataLayer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.largeblueberry.setting.ui.theme.domain.ThemeOption
import com.largeblueberry.setting.ui.theme.domain.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


// Context.dataStore를 최상위 속성으로 정의하여 앱 전체에서 싱글톤으로 사용
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemeRepositoryImpl(private val context: Context) : ThemeRepository {

    private object PreferencesKeys {
        val THEME_OPTION = stringPreferencesKey("theme_option")
    }

    override val themeOption: Flow<ThemeOption> = context.dataStore.data
        .map { preferences ->
            val themeKey = preferences[PreferencesKeys.THEME_OPTION]
            ThemeOption.fromKey(themeKey)
        }

    override suspend fun saveThemeOption(theme: ThemeOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_OPTION] = theme.key
        }
    }
}