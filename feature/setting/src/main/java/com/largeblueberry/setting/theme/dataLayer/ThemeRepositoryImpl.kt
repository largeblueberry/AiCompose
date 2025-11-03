package com.largeblueberry.setting.ui.theme.dataLayer

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.largeblueberry.core_ui.ThemeOption
import com.largeblueberry.setting.ui.theme.domain.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Named

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    @Named("theme") private val dataStore: DataStore<Preferences>
) : ThemeRepository {

    private val themeKey = stringPreferencesKey("theme_option")

    override fun getThemeOption(): Flow<ThemeOption> {
        return dataStore.data.map { preferences ->
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
        dataStore.edit { preferences ->
            preferences[themeKey] = option.key // name -> key로 변경
        }
    }
}
