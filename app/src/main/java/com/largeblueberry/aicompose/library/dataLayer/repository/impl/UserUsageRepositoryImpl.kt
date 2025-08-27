package com.largeblueberry.aicompose.library.dataLayer.repository.impl

import android.content.SharedPreferences
import com.largeblueberry.usertracker.repository.UserUsageRepository
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class UserUsageRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : UserUsageRepository {

    companion object {
        private const val ANONYMOUS_USER_KEY = "anonymous_upload_count"
        private const val USER_UPLOAD_COUNT_PREFIX = "user_upload_count_"
    }

    override suspend fun getCurrentUploadCount(userId: String?): Int {
        val key = if (userId == null) {
            ANONYMOUS_USER_KEY
        } else {
            "$USER_UPLOAD_COUNT_PREFIX$userId"
        }
        return sharedPreferences.getInt(key, 0)
    }

    override suspend fun incrementUploadCount(userId: String?) {
        val key = if (userId == null) {
            ANONYMOUS_USER_KEY
        } else {
            "$USER_UPLOAD_COUNT_PREFIX$userId"
        }
        val currentCount = sharedPreferences.getInt(key, 0)
        sharedPreferences.edit {
            putInt(key, currentCount + 1)
        }
    }

    override suspend fun resetUploadCount(userId: String?) {
        val key = if (userId == null) {
            ANONYMOUS_USER_KEY
        } else {
            "$USER_UPLOAD_COUNT_PREFIX$userId"
        }
        sharedPreferences.edit {
            putInt(key, 0)
        }
    }
}