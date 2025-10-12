package com.largeblueberry.setting.ui.theme.domain

import com.largeblueberry.core_ui.ThemeOption
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    /**
     * 현재 설정된 테마 옵션을 Flow 형태로 가져옵니다.
     * DataStore의 값이 변경될 때마다 새로운 값을 발행합니다.
     */
    fun getThemeOption(): Flow<ThemeOption>

    /**
     * 새로운 테마 옵션을 저장합니다.
     */
    suspend fun setThemeOption(option: ThemeOption)
}