package com.largeblueberry.core_ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Material 3의 표준 색상 시스템에 맞게 매핑한 라이트 모드 색상표
private val AppLightColorScheme = lightColorScheme(
    primary = LightColors.AppPrimaryBlue,
    onPrimary = LightColors.AppWhite, // Primary 색상 위에 표시될 콘텐츠 색상
    background = LightColors.AppBackground,
    onBackground = LightColors.AppTextDark,
    surface = LightColors.AppWhite, // 카드, 다이얼로그 등의 배경
    onSurface = LightColors.AppTextDark, // Surface 위의 콘텐츠 색상
    error = LightColors.AppRed,
    onError = LightColors.AppWhite, // Error 색상 위의 콘텐츠 색상

    outline = LightColors.googleButtonBorderColor, // 테두리 색상
    surfaceVariant = LightColors.googleDisabledContainerColor, // 약간 다른 톤의 Surface (예: 비활성 컴포넌트 배경)
    onSurfaceVariant = LightColors.googleDisabledContentColor // SurfaceVariant 위의 콘텐츠 (예: 비활성 컴포넌트 텍스트)
)


private val AppDarkColorScheme = darkColorScheme(
    primary = DarkColors.AppPrimaryBlue,
    onPrimary = DarkColors.AppBlack, // Primary 색상 위의 콘텐츠 색상 (흰색)
    background = DarkColors.AppBackground,
    onBackground = DarkColors.AppTextDark,
    surface = DarkColors.SettingBackground, // 카드, 다이얼로그 등의 배경 (일반적인 어두운 배경)
    onSurface = DarkColors.AppTextDark, // Surface 위의 콘텐츠 색상
    error = DarkColors.AppRed,
    onError = DarkColors.AppBlack, // Error 색상 위의 콘텐츠 색상 (흰색)

    outline = DarkColors.GoogleButtonBorderColor, // 테두리 색상
    surfaceVariant = DarkColors.GoogleDisabledContainerColor, // 비활성 컴포넌트 배경
    onSurfaceVariant = DarkColors.GoogleDisabledContentColor // 비활성 컴포넌트 텍스트
)


data class CustomColors(
    val cardViewMainText: Color,
    val cardViewSubText: Color,
    val cardViewBackground: Color,
    val recordingRed: Color,
    val progressBarBackground: Color,
    val settingUtilColor: Color,
    val settingItemMainText: Color,
    val utilTextColor: Color,
    val settingBackground: Color,
    val settingBasicUser: Color,
    val utilHorizontalDivider: Color,
    val appWhite : Color,
    val appBlack : Color,
    val titleBlueDark : Color,
    val subtitleBlue : Color
)


// 라이트 모드일 때 사용할 CustomColors 객체
val LightCustomColors = CustomColors(
    cardViewMainText = LightColors.CardViewMainText,
    cardViewSubText = LightColors.CardViewSubText,
    cardViewBackground = LightColors.CardViewBackGround,
    recordingRed = LightColors.RecordingRed,
    progressBarBackground = LightColors.ProgressBarBackgroundTint,
    settingUtilColor = LightColors.SettingUtilColor,
    settingItemMainText = LightColors.SettingItemMainText,
    utilTextColor = LightColors.UtilTextColor,
    settingBackground = LightColors.SettingBackground,
    settingBasicUser = LightColors.SettingBasicUser,
    utilHorizontalDivider = LightColors.utilHorizontalDivider,
    appWhite = LightColors.AppWhite,
    appBlack = LightColors.AppBlack,
    titleBlueDark = LightColors.TitleBlueDark,
    subtitleBlue = LightColors.SubtitleBlue
)

// 다크 모드일 때 사용할 CustomColors 객체
val DarkCustomColors = CustomColors(
    cardViewMainText = DarkColors.CardViewMainText,
    cardViewSubText = DarkColors.CardViewSubText,
    cardViewBackground = DarkColors.CardViewBackGround,
    recordingRed = DarkColors.RecordingRed,
    progressBarBackground = DarkColors.ProgressBarBackgroundTint,
    settingUtilColor = DarkColors.SettingUtilColor,
    settingItemMainText = DarkColors.SettingItemMainText,
    utilTextColor = DarkColors.UtilTextColor,
    settingBackground = DarkColors.SettingBackground,
    settingBasicUser = DarkColors.SettingBasicUser,
    utilHorizontalDivider = DarkColors.UtilHorizontalDivider,
    appWhite = LightColors.AppBlack,
    appBlack = LightColors.AppWhite,
    titleBlueDark = LightColors.TitleBlueDark,
    subtitleBlue = LightColors.SubtitleBlue
)

// CompositionLocal을 통해 커스텀 색상을 앱 전체에 제공
val LocalCustomColors = staticCompositionLocalOf { LightCustomColors }


@Composable
fun AppTheme(
    themeOption: ThemeOption,
    content: @Composable () -> Unit
) {
    // 현재 테마에 맞는 표준 색상(colorScheme)과 커스텀 색상(customColors)을 선택
    val (colorScheme, customColors) = when (themeOption) {
        ThemeOption.LIGHT -> AppLightColorScheme to LightCustomColors
        ThemeOption.DARK -> AppDarkColorScheme to DarkCustomColors
        ThemeOption.SYSTEM -> if (isSystemInDarkTheme()) {
            AppDarkColorScheme to DarkCustomColors
        } else {
            AppLightColorScheme to LightCustomColors
        }
    }

    // CompositionLocalProvider로 customColors를 앱 하위 전체에 제공
    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme, // 표준 색상 적용
            typography = Typography,
            content = content
        )
    }
}

val MaterialTheme.customColors: CustomColors
    @Composable
    get() = LocalCustomColors.current