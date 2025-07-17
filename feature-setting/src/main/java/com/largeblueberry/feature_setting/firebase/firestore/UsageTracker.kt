package com.largeblueberry.feature_setting.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.largeblueberry.feature_setting.firebase.firestore.model.UsageData
import com.largeblueberry.feature_setting.firebase.auth.UsageLimitResult
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageTracker @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val usageCollection = firestore.collection("usage")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun checkUsageLimit(userId: String): UsageLimitResult {
        return try {
            val today = dateFormat.format(Date())
            val usageDoc = usageCollection.document(userId).get().await()

            val usageData = if (usageDoc.exists()) {
                usageDoc.toObject(UsageData::class.java) ?: UsageData(userId = userId)
            } else {
                UsageData(userId = userId)
            }

            // 날짜가 바뀌면 일일 사용량 리셋
            val resetUsageData = if (usageData.lastUsageDate != today) {
                usageData.copy(
                    dailyUsageCount = 0,
                    lastResetDate = today
                )
            } else {
                usageData
            }

            val canUse = resetUsageData.dailyUsageCount < UsageData.DAILY_LIMIT_FREE
            val remainingCount = UsageData.DAILY_LIMIT_FREE - resetUsageData.dailyUsageCount

            UsageLimitResult(
                canUse = canUse,
                remainingCount = maxOf(0, remainingCount),
                dailyLimit = UsageData.DAILY_LIMIT_FREE,
                message = if (canUse) {
                    "오늘 ${remainingCount}회 더 사용 가능합니다."
                } else {
                    "일일 사용 제한(${UsageData.DAILY_LIMIT_FREE}회)에 도달했습니다. 내일 다시 이용해주세요."
                }
            )

        } catch (e: Exception) {
            UsageLimitResult(
                canUse = false,
                remainingCount = 0,
                dailyLimit = UsageData.DAILY_LIMIT_FREE,
                message = "사용량 확인 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    suspend fun incrementUsage(userId: String): Boolean {
        return try {
            val today = dateFormat.format(Date())
            val usageDocRef = usageCollection.document(userId)
            val usageDoc = usageDocRef.get().await()

            val currentUsage = if (usageDoc.exists()) {
                usageDoc.toObject(UsageData::class.java) ?: UsageData(userId = userId)
            } else {
                UsageData(userId = userId)
            }

            // 날짜가 바뀌면 일일 사용량 리셋
            val updatedUsage = if (currentUsage.lastUsageDate != today) {
                currentUsage.copy(
                    dailyUsageCount = 1,
                    totalUsageCount = currentUsage.totalUsageCount + 1,
                    lastUsageDate = today,
                    lastResetDate = today,
                    updatedAt = com.google.firebase.Timestamp.now()
                )
            } else {
                currentUsage.copy(
                    dailyUsageCount = currentUsage.dailyUsageCount + 1,
                    totalUsageCount = currentUsage.totalUsageCount + 1,
                    lastUsageDate = today,
                    updatedAt = com.google.firebase.Timestamp.now()
                )
            }

            usageDocRef.set(updatedUsage).await()
            true

        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserUsageData(userId: String): UsageData? {
        return try {
            val usageDoc = usageCollection.document(userId).get().await()
            if (usageDoc.exists()) {
                usageDoc.toObject(UsageData::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}