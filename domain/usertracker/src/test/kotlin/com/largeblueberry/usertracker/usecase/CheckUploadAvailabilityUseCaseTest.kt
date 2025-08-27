package com.largeblueberry.usertracker.usecase

import com.largeblueberry.usertracker.model.UploadAvailabilityResult
import com.largeblueberry.usertracker.repository.UserUsageRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class CheckUploadAvailabilityUseCaseTest {

    // Mocking UserUsageRepository
    @Mock
    private lateinit var userUsageRepository: UserUsageRepository

    // SUT (System Under Test)
    private lateinit var checkUploadAvailabilityUseCase: CheckUploadAvailabilityUseCase

    // 각 테스트가 실행되기 전에 초기화
    @Before
    fun setUp() {

        checkUploadAvailabilityUseCase = CheckUploadAvailabilityUseCase(
            userUsageRepository = userUsageRepository
        )
    }

    @Test
    fun `로그인 한 사용자가 업로드 가능 횟수가 남아있는 경우`() = runTest {
        // given
        val userId = "testUser"
        val currentCount = 3

        `when`(userUsageRepository.isLoggedIn()).thenReturn(true)
        `when`(userUsageRepository.getCurrentUploadCount(userId))
            .thenReturn(currentCount)

        // when
        val result = checkUploadAvailabilityUseCase.invoke(userId)
        // then
        assertTrue(result is UploadAvailabilityResult.Available)
        assertEquals(UploadAvailabilityResult.Available(remainingUploads = 5 - currentCount), result)
        // 남은 횟수 검증
    }
    @Test
    fun `로그인 한 사용자가 업로드 한도에 도달한 경우`() = runTest {
        // Given
        val userId = "testUser123"
        whenever(userUsageRepository.isLoggedIn()).thenReturn(true)
        whenever(userUsageRepository.getCurrentUploadCount(userId)).thenReturn(5) // 현재 5회 업로드 (최대)

        // When
        val result = checkUploadAvailabilityUseCase.invoke(userId)

        // Then
        // UploadAvailabilityResult.LimitReached(maxUploads = 5) 가 반환되어야 함
        assertEquals(UploadAvailabilityResult.LimitReached(maxUploads = 5), result)
    }

    @Test
    fun `익명 사용자가 업로드 가능 횟수가 남아있는 경우`() = runTest {
        // Given
        val userId: String? = null // 익명 사용자
        whenever(userUsageRepository.isLoggedIn()).thenReturn(false) // 비로그인 상태로 가정
        whenever(userUsageRepository.getCurrentUploadCount(userId)).thenReturn(0) // 현재 0회 업로드

        // When
        val result = checkUploadAvailabilityUseCase.invoke(userId)

        // Then
        // UploadAvailabilityResult.Available(remainingUploads = 1 - 0 = 1) 가 반환되어야 함
        assertEquals(UploadAvailabilityResult.Available(remainingUploads = 1), result)
    }

    @Test
    fun `익명 사용자가 업로드 한도에 도달한 경우`() = runTest {
        // Given
        val userId: String? = null
        whenever(userUsageRepository.isLoggedIn()).thenReturn(false)
        whenever(userUsageRepository.getCurrentUploadCount(userId)).thenReturn(1) // 현재 1회 업로드 (최대)

        // When
        val result = checkUploadAvailabilityUseCase.invoke(userId)

        // Then
        // UploadAvailabilityResult.LimitReached(maxUploads = 1) 가 반환되어야 함
        assertEquals(UploadAvailabilityResult.LimitReached(maxUploads = 1), result)
    }

    @Test
    fun `uploadCounter 메서드 호출 시 incrementUploadCount가 호출되는지 확인`() = runTest {
        // Given
        val userId = "someUser"

        // When
        checkUploadAvailabilityUseCase.uploadCounter(userId)

        // Then
        // userUsageRepository의 incrementUploadCount 메서드가 userId 인자와 함께 한 번 호출되었는지 검증
        verify(userUsageRepository).incrementUploadCount(userId)
    }

    @Test
    fun `resetCounter 메서드 호출 시 resetUploadCount가 호출되는지 확인`() = runTest {
        // Given
        val userId = "someUser"

        // When
        checkUploadAvailabilityUseCase.resetCounter(userId)

        // Then
        // userUsageRepository의 resetUploadCount 메서드가 userId 인자와 함께 한 번 호출되었는지 검증
        verify(userUsageRepository).resetUploadCount(userId)
    }
}