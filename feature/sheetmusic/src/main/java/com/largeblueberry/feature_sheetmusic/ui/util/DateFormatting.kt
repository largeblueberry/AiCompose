package com.largeblueberry.feature_sheetmusic.ui.util

import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * 생성 시각(epoch ms) → 사용자 언어에 맞는 날짜·시간 문자열.
 * 저장은 숫자로 하고, 표시 형식은 화면이 정한다(docs/troubleshooting.md C-1).
 */
internal fun formatCreatedAt(epochMillis: Long, locale: Locale = Locale.getDefault()): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale).format(Date(epochMillis))
