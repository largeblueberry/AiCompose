package com.largeblueberry.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.largeblueberry.domain.model.Tempo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesRepositoryImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = CoroutineScope(UnconfinedTestDispatcher() + SupervisorJob())

    private val repository by lazy {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { tmp.newFile("user_preferences.preferences_pb").also { it.delete() } },
        )
        UserPreferencesRepositoryImpl(dataStore)
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun lastBpmDefaultsWhenNeverSaved() = runTest {
        assertEquals(Tempo.DEFAULT_BPM, repository.lastBpm.first())
    }

    @Test
    fun savedBpmIsReadBack() = runTest {
        repository.setLastBpm(135)
        assertEquals(135, repository.lastBpm.first())
    }

    // 테스트마다 저장은 한 번만 한다. JVM DataStore는 임시 파일을 원래 이름으로 바꿔 저장하는데,
    // Windows에서는 대상 파일이 이미 있으면 이름 바꾸기가 실패한다(기기·Linux CI에서는 문제없음).

    @Test
    fun tooFastBpmIsClampedToMax() = runTest {
        repository.setLastBpm(1_000)
        assertEquals(Tempo.MAX_BPM, repository.lastBpm.first())
    }

    @Test
    fun tooSlowBpmIsClampedToMin() = runTest {
        repository.setLastBpm(1)
        assertEquals(Tempo.MIN_BPM, repository.lastBpm.first())
    }

    @Test
    fun onboardingFlagStillWorks() = runTest {
        assertEquals(false, repository.isOnboardingCompleted.first())
        repository.setOnboardingCompleted(true)
        assertEquals(true, repository.isOnboardingCompleted.first())
    }
}
