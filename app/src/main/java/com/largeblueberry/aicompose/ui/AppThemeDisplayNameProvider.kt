package com.largeblueberry.aicompose.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.largeblueberry.resources.R
import com.largeblueberry.core_ui.ThemeDisplayNameProvider
import com.largeblueberry.core_ui.ThemeOption
import javax.inject.Inject

/**
 * :core_ui에 정의된 명세서의 실제 구현체.
 * app 모듈의 리소스를 사용하여 다국어 지원 텍스트를 반환한다.
 * (원래 toDisplayName() 함수에 있던 로직이 여기로 온다고 생각하시면 됩니다)
 */
class AppThemeDisplayNameProvider @Inject constructor() : ThemeDisplayNameProvider {
    @Composable
    override fun getDisplayName(themeOption: ThemeOption): String {
        return when (themeOption) {
            ThemeOption.LIGHT -> stringResource(R.string.theme_light)
            ThemeOption.DARK -> stringResource(R.string.theme_dark)
            ThemeOption.SYSTEM -> stringResource(R.string.theme_system)
        }
    }
}