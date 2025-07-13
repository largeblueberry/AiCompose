package com.largeblueberry.feature_setting.firebase.firestore.model
import com.google.firebase.Timestamp

data class UsageData(
    val userId: String = "",
    val dailyUsageCount: Int = 0,
    val totalUsageCount: Int = 0,
    val lastUsageDate: String = "", // YYYY-MM-DD format
    val lastResetDate: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    companion object {
        const val DAILY_LIMIT_FREE = 5 // 무료 사용자 일일 제한
        const val DAILY_LIMIT_PREMIUM = 100 // 프리미엄 사용자 일일 제한
    }
}
