package com.largeblueberry.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)

    /** 마지막으로 고른 녹음 BPM. 없으면 기본값. */
    val lastBpm: Flow<Int>
    suspend fun setLastBpm(bpm: Int)
}
