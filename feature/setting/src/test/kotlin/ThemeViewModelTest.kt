package com.largeblueberry.setting.presentation

import com.largeblueberry.core_ui.ThemeOption
import com.largeblueberry.setting.theme.domain.ThemeRepository
import com.largeblueberry.setting.theme.ui.ThemeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ThemeViewModelTest {

    private lateinit var mockThemeRepository: ThemeRepository
    private lateinit var themeViewModel: ThemeViewModel
    private val testDispatcher = StandardTestDispatcher() // 테스트용 코루틴 디스패처
    private lateinit var themeOptionFlow: MutableStateFlow<ThemeOption> // Repository의 Flow를 모의하기 위한 Flow

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher) // Main 디스패처를 테스트 디스패처로 설정
        themeOptionFlow = MutableStateFlow(ThemeOption.SYSTEM) // Repository가 반환할 초기 테마 설정

        mockThemeRepository = mockk {
            // getThemeOption() 호출 시 themeOptionFlow를 반환하도록 설정
            every { getThemeOption() } returns themeOptionFlow
            // setThemeOption() 호출 시 아무것도 하지 않도록 설정 (suspend 함수)
            coEvery { setThemeOption(any()) } just runs
        }
        // ViewModel 초기화 (mock된 Repository 주입)
        themeViewModel = ThemeViewModel(mockThemeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Main 디스패처를 원래대로 복원
    }

    @Test
    fun `init block collects initial theme from repository`() = runTest(testDispatcher) {
        // ViewModel 초기화 후, uiState의 selectedTheme가 Repository의 초기값(SYSTEM)과 일치하는지 확인
        assertEquals(ThemeOption.SYSTEM, themeViewModel.uiState.value.selectedTheme)

        // Repository의 테마가 LIGHT로 변경된 상황을 시뮬레이션
        themeOptionFlow.value = ThemeOption.LIGHT
        advanceUntilIdle() // 코루틴이 작업을 완료할 때까지 대기

        // uiState가 업데이트되었는지 확인
        assertEquals(ThemeOption.LIGHT, themeViewModel.uiState.value.selectedTheme)
    }

    @Test
    fun `onThemeSelected calls repository to set theme and updates uiState`() = runTest(testDispatcher) {
        // 초기 상태 확인
        assertEquals(ThemeOption.SYSTEM, themeViewModel.uiState.value.selectedTheme)

        // DARK 테마 선택
        themeViewModel.onThemeSelected(ThemeOption.DARK)
        advanceUntilIdle() // 코루틴이 작업을 완료할 때까지 대기

        // Repository의 setThemeOption 함수가 DARK 옵션으로 호출되었는지 확인
        coVerify(exactly = 1) { mockThemeRepository.setThemeOption(ThemeOption.DARK) }

        // Repository가 테마를 DARK로 업데이트했다고 가정하고 Flow를 변경
        themeOptionFlow.value = ThemeOption.DARK
        advanceUntilIdle()

        // uiState가 DARK로 업데이트되었는지 확인
        assertEquals(ThemeOption.DARK, themeViewModel.uiState.value.selectedTheme)
    }

    @Test
    fun `uiState updates when theme option changes in repository`() = runTest(testDispatcher) {
        // 초기 상태 확인
        assertEquals(ThemeOption.SYSTEM, themeViewModel.uiState.value.selectedTheme)

        // Repository의 테마가 LIGHT로 변경된 상황을 시뮬레이션
        themeOptionFlow.value = ThemeOption.LIGHT
        advanceUntilIdle()

        // uiState가 LIGHT로 업데이트되었는지 확인
        assertEquals(ThemeOption.LIGHT, themeViewModel.uiState.value.selectedTheme)

        // Repository의 테마가 DARK로 다시 변경된 상황을 시뮬레이션
        themeOptionFlow.value = ThemeOption.DARK
        advanceUntilIdle()

        // uiState가 DARK로 업데이트되었는지 확인
        assertEquals(ThemeOption.DARK, themeViewModel.uiState.value.selectedTheme)
    }
}
