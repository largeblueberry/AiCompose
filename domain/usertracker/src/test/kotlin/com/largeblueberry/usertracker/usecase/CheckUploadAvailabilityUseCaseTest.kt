package com.largeblueberry.usertracker.usecase

import com.largeblueberry.usertracker.model.UploadAvailabilityResult
import com.largeblueberry.usertracker.repository.AuthGateway
import com.largeblueberry.usertracker.repository.UserUsageRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
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

    @Mock
    private lateinit var authGateway: AuthGateway

    // SUT (System Under Test)
    private lateinit var checkUploadAvailabilityUseCase: CheckUploadAvailabilityUseCase

    // 각 테스트가 실행되기 전에 초기화
    @Before
    fun setUp() {

        checkUploadAvailabilityUseCase = CheckUploadAvailabilityUseCase(
            userUsageRepository = userUsageRepository,
            authGateway = authGateway
        )
    }

    @Test
    fun `로그인 한 사용자가 업로드 가능 횟수가 남아있는 경우`() = runTest {
        // given
        val userId = "testUser"
        val currentCount = 3
        val maxUpload = 5

        whenever(authGateway.isLoggedIn()).thenReturn(true)
        whenever(authGateway.getCurrentUserId()).thenReturn(userId)
        whenever(authGateway.getUploadLimitForUser(userId)).thenReturn(maxUpload)
        whenever(userUsageRepository.getCurrentUploadCount(userId)).thenReturn(currentCount)

        // when
        val result = checkUploadAvailabilityUseCase.invoke()
        // then
        assertTrue(result is UploadAvailabilityResult.Available)
        val availableResult = result
        assertEquals(2, availableResult.remainingUploads) // 5 - 3 = 2
        assertEquals(3, availableResult.currentUploads)
        assertEquals(5, availableResult.maxUploads)
    }
    @Test
    fun `로그인 한 사용자가 업로드 한도에 도달한 경우`() = runTest {
        // Given
        val userId = "testUser123"
        val maxUpload = 5

        whenever(authGateway.isLoggedIn()).thenReturn(true)
        whenever(authGateway.getCurrentUserId()).thenReturn(userId)
        whenever(authGateway.getUploadLimitForUser(userId)).thenReturn(maxUpload)
        whenever(userUsageRepository.getCurrentUploadCount(userId)).thenReturn(5)
        // When
        val result = checkUploadAvailabilityUseCase.invoke()

        // Then
        assertTrue(result is UploadAvailabilityResult.LimitReached)
        val limitResult = result
        assertEquals(5, limitResult.maxUploads)
        assertEquals(5, limitResult.currentUploads)
    }

    @Test
    fun `익명 사용자가 업로드 가능 횟수가 남아있는 경우`() = runTest {
        // Given
        val userId: String? = null
        val maxUpload = 1

        whenever(authGateway.isLoggedIn()).thenReturn(false)
        whenever(authGateway.getCurrentUserId()).thenReturn(userId)
        whenever(authGateway.getUploadLimitForUser(userId)).thenReturn(maxUpload)
        whenever(userUsageRepository.getCurrentUploadCount(userId)).thenReturn(0)

        // When
        val result = checkUploadAvailabilityUseCase.invoke()

        // Then
        assertTrue(result is UploadAvailabilityResult.Available)
        val availableResult = result
        assertEquals(1, availableResult.remainingUploads) // 1 - 0 = 1
        assertEquals(0, availableResult.currentUploads)
        assertEquals(1, availableResult.maxUploads)
    }

    @Test
    fun `익명 사용자가 업로드 한도에 도달한 경우`() = runTest {
        // Given
        val userId: String? = null
        val maxUpload = 1

        whenever(authGateway.isLoggedIn()).thenReturn(false)
        whenever(authGateway.getCurrentUserId()).thenReturn(userId)
        whenever(authGateway.getUploadLimitForUser(userId)).thenReturn(maxUpload)
        whenever(userUsageRepository.getCurrentUploadCount(userId)).thenReturn(1)

        // When
        val result = checkUploadAvailabilityUseCase.invoke()


        // Then
        assertTrue(result is UploadAvailabilityResult.LimitReached)
        val limitResult = result
        assertEquals(1, limitResult.maxUploads)
        assertEquals(1, limitResult.currentUploads)
    }

    @Test
    fun `uploadCounter 메서드 호출 시 incrementUploadCount가 호출되는지 확인`() = runTest {
        // Given
        val userId = "someUser"

        whenever(authGateway.getCurrentUserId()).thenReturn(userId)

        // When
        checkUploadAvailabilityUseCase.uploadCounter()

        // Then
        verify(userUsageRepository).incrementUploadCount(userId)
    }

    @Test
    fun `resetCounter 메서드 호출 시 resetUploadCount가 호출되는지 확인`() = runTest {
        // Given
        val userId = "someUser"

        whenever(authGateway.getCurrentUserId()).thenReturn(userId)

        // When
        checkUploadAvailabilityUseCase.resetCounter()

        // Then
        verify(userUsageRepository).resetUploadCount(userId)
    }
}