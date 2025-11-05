package com.largeblueberry.data.repository

import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    fun getLanguageCode(): Flow<String>
    suspend fun setLanguageCode(languageCode: String)
}
