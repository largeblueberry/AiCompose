package com.largeblueberry.core_ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.largeblueberry.resources.R


enum class ThemeOption(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        /**
         * 저장된 key 문자열에 해당하는 ThemeOption 객체를 찾아서 반환합니다.
         * 해당하는 key가 없으면 SYSTEM을 기본값으로 사용합니다.
         */
        fun fromKey(key: String?): ThemeOption {
            return entries.find { it.key == key } ?: SYSTEM
        }
    }
}

/**
 * 각 ThemeOption에 맞는 표시 이름을 string resource로부터 가져오는 Composable.
 * (다국어 지원을 위해 하드코딩 대신 리소스 사용을 권장합니다)
 */
@Composable
fun ThemeOption.toDisplayName(): String {
    return when (this) {
        ThemeOption.LIGHT -> stringResource(R.string.theme_light)
        ThemeOption.DARK -> stringResource(R.string.theme_dark)
        ThemeOption.SYSTEM -> stringResource(R.string.theme_system)
    }
}