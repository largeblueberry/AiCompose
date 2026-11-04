package com.largeblueberry.domain.repository

import com.largeblueberry.domain.model.ThemeOption
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun getThemeOption(): Flow<ThemeOption>
    suspend fun setThemeOption(option: ThemeOption)
}
