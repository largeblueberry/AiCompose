package com.largeblueberry.aicompose.ui.onboarding

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.largeblueberry.resources.R

// Sealed class로 온보딩 페이지 정의
sealed class OnboardingPageData(
    val icon: String,
    @StringRes val titleResId: Int, // Resource ID로 변경
    @StringRes val descriptionResId: Int, // Resource ID로 변경
    @StringRes val buttonTextResId: Int = R.string.onboarding_default_button_text,
    val buttonColor: @Composable () -> Color
) {
    // 1단계: 문제 공감
    object ProblemSolution : OnboardingPageData(
        icon = "😤",
        titleResId = R.string.onboarding_problem_solution_title,
        descriptionResId = R.string.onboarding_problem_solution_description,
        buttonTextResId = R.string.onboarding_problem_solution_button_text,
        buttonColor = { MaterialTheme.colorScheme.primary }
    )
    // 2단계: 권한 요청
    object PermissionRequest : OnboardingPageData(
        icon = "🛡️",
        titleResId = R.string.onboarding_permission_request_title,
        descriptionResId = R.string.onboarding_permission_request_description,
        buttonTextResId = R.string.onboarding_permission_request_button_text,
        buttonColor = { MaterialTheme.colorScheme.primary }
    )

    // 3단계: 권한 허용 완료
    object PermissionSuccess : OnboardingPageData(
        icon = "⚡",
        titleResId = R.string.onboarding_permission_success_title,
        descriptionResId = R.string.onboarding_permission_success_description,
        buttonTextResId = R.string.onboarding_permission_success_button_text,
        buttonColor = { MaterialTheme.colorScheme.primary }
    )

    /**
     * 문제 의식 -> 권한 요청 -> 권한 허용 완료
     */
    companion object {
        // 권한 허용 경로: 1 -> 2 -> 3
        val permissionPath = listOf(ProblemSolution, PermissionRequest, PermissionSuccess)

        fun getPageData(index: Int): OnboardingPageData {
            val currentPath = permissionPath
            return when {
                index < currentPath.size -> currentPath[index]
                else -> ProblemSolution // 기본값
            }
        }

        fun getPageCount(): Int {
            return permissionPath.size
        }

        // 권한 요청 페이지인지 확인
        fun isPermissionRequestPage(index: Int): Boolean {
            return index == 1 // PermissionRequest는 2번째 페이지 (index 1)
        }

        // 마지막 페이지인지 확인
        fun isLastPage(index: Int): Boolean {
            return index == getPageCount() - 1
        }
    }
}