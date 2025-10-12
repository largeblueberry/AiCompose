package com.largeblueberry.setting.ui.language.domain

import kotlinx.coroutines.flow.Flow

interface LanguageRepository {

    /**
     * 현재 설정된 언어 코드를 Flow 형태로 가져옴.
     * 값이 없을 경우 시스템 기본 언어 코드를 반환
     */
    val language: Flow<String>

    /**
     * 새로운 언어 코드를 저장
     * @param languageCode 저장할 언어 코드 (예: "en", "ko")
     */
    suspend fun setLanguage(languageCode: String)
}