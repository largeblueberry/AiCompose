package com.largeblueberry.library

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.largeblueberry.library.ui.screen.LibraryScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class LibraryScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @MockK
    private lateinit var mockNavController: NavController

    @MockK
    private lateinit var mockOnUploadSuccess: (String) -> Unit

    @MockK
    private lateinit var mockOnBackClick: () -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        hiltRule.inject()
    }

    @Test
    fun libraryScreen_displaysTitle() {
        // Given
        composeTestRule.setContent {
            LibraryScreen(
                onUploadSuccess = mockOnUploadSuccess,
                navController = mockNavController,
                onBackClick = mockOnBackClick
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("내 라이브러리") // 실제 문자열로 변경
            .assertIsDisplayed()
    }

    @Test
    fun libraryScreen_displaysBackButton() {
        // Given
        composeTestRule.setContent {
            LibraryScreen(
                onUploadSuccess = mockOnUploadSuccess,
                navController = mockNavController,
                onBackClick = mockOnBackClick
            )
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("뒤로가기") // 실제 contentDescription으로 변경
            .assertIsDisplayed()
    }

    @Test
    fun backButton_callsOnBackClick_whenNoBackStack() {
        // Given
        every { mockNavController.previousBackStackEntry } returns null

        composeTestRule.setContent {
            LibraryScreen(
                onUploadSuccess = mockOnUploadSuccess,
                navController = mockNavController,
                onBackClick = mockOnBackClick
            )
        }

        // When
        composeTestRule
            .onNodeWithContentDescription("뒤로가기")
            .performClick()

        // Then
        verify { mockOnBackClick() }
    }
}