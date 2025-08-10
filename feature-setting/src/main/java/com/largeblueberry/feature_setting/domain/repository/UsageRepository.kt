package com.largeblueberry.feature_setting.domain.repository

import com.largeblueberry.feature_setting.firebase.auth.UsageLimitResult
import com.largeblueberry.feature_setting.firebase.firestore.model.UsageData

interface UsageRepository {
    suspend fun checkUsageLimit(userId: String): UsageLimitResult
    suspend fun incrementUsage(userId: String): Boolean
    suspend fun getUserUsageData(userId: String): UsageData?
}