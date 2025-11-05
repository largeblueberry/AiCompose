package com.largeblueberry.core_ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun stringResource(@StringRes id: Int): String {
    val context = LocalContext.current
    val languageCode = LocalLanguageCode.current

    return remember(id, languageCode) {
        context.getString(id)
    }
}

@Composable
fun stringResource(@StringRes id: Int, vararg formatArgs: Any): String {
    val context = LocalContext.current
    val languageCode = LocalLanguageCode.current

    return remember(id, languageCode, *formatArgs) {
        context.getString(id, *formatArgs)
    }
}