package com.largeblueberry.core_ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val AppBackground: Color
        @Composable get() = if (isDarkTheme()) DarkColors.AppBackground else LightColors.AppBackground

    val AppWhite: Color
        @Composable get() = if (isDarkTheme()) DarkColors.AppWhite else LightColors.AppWhite

    val AppBlack: Color
        @Composable get() = if (isDarkTheme()) DarkColors.AppBlack else LightColors.AppBlack

    val AppRed: Color
        @Composable get() = if (isDarkTheme()) DarkColors.AppRed else LightColors.AppRed

    val AppPrimaryBlue: Color
        @Composable get() = if (isDarkTheme()) DarkColors.AppPrimaryBlue else LightColors.AppPrimaryBlue

    // 텍스트 색상
    val AppTextDark: Color
        @Composable get() = if (isDarkTheme()) DarkColors.AppTextDark else LightColors.AppTextDark

    val CardViewMainText: Color
        @Composable get() = if (isDarkTheme()) DarkColors.CardViewMainText else LightColors.CardViewMainText

    val CardViewSubText: Color
        @Composable get() = if (isDarkTheme()) DarkColors.CardViewSubText else LightColors.CardViewSubText

    // 특정 목적의 색상
    val RecordingRed: Color
        @Composable get() = if (isDarkTheme()) DarkColors.RecordingRed else LightColors.RecordingRed

    val ProgressBarBackgroundTint: Color
        @Composable get() = if (isDarkTheme()) DarkColors.ProgressBarBackgroundTint else LightColors.ProgressBarBackgroundTint

    val CardViewBackGround: Color
        @Composable get() = if (isDarkTheme()) DarkColors.CardViewBackGround else LightColors.CardViewBackGround

    // Setting screen colors
    val SettingUtilColor: Color
        @Composable get() = if (isDarkTheme()) DarkColors.SettingUtilColor else LightColors.SettingUtilColor

    val SettingItemMainText: Color
        @Composable get() = if (isDarkTheme()) DarkColors.SettingItemMainText else LightColors.SettingItemMainText

    val UtilTextColor: Color
        @Composable get() = if (isDarkTheme()) DarkColors.UtilTextColor else LightColors.UtilTextColor

    val SettingBackground: Color
        @Composable get() = if (isDarkTheme()) DarkColors.SettingBackground else LightColors.SettingBackground

    val SettingBasicUser: Color
        @Composable get() = if (isDarkTheme()) DarkColors.SettingBasicUser else LightColors.SettingBasicUser

    // Login screen colors
    val GoogleDisabledContainerColor: Color
        @Composable get() = if (isDarkTheme()) DarkColors.GoogleDisabledContainerColor else LightColors.googleDisabledContainerColor

    val GoogleDisabledContentColor: Color
        @Composable get() = if (isDarkTheme()) DarkColors.GoogleDisabledContentColor else LightColors.googleDisabledContentColor

    val GoogleButtonBorderColor: Color
        @Composable get() = if (isDarkTheme()) DarkColors.GoogleButtonBorderColor else LightColors.googleButtonBorderColor

    val UtilHorizontalDivider: Color
        @Composable get() = if (isDarkTheme()) DarkColors.UtilHorizontalDivider else LightColors.utilHorizontalDivider
}