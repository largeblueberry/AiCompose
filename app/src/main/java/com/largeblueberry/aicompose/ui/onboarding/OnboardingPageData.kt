package com.largeblueberry.aicompose.ui.onboarding

// Sealed class로 온보딩 페이지 정의
sealed class OnboardingPageData(
    val icon: String,
    val title: String,
    val description: String,
    val backgroundColor: String = "#FFFFFF", // 배경색 추가
    val buttonText: String = "다음 >",
    val buttonColor: String = "#4A90E2"
) {
    // 1단계: 문제 공감 (빨간색 테마)
    object ProblemSolution : OnboardingPageData(
        icon = "😤",
        title = "순간 떠오르는 악상 기록하기 힘들었나요?",
        description = "순간의 영감을 빨리 기록하세요!",
        backgroundColor = "#FEF2F2", // red-50
        buttonText = "시작하기",
        buttonColor = "#EF4444" // red-500
    )

    // 2단계: 신뢰 구축 (초록색 테마)
    object PermissionRequest : OnboardingPageData(
        icon = "🛡️",
        title = "접근성 권한이 필요해요",
        description = "녹음을 위해서는 마이크 권한이 필요해요",
        backgroundColor = "#F0FDF4", // green-50
        buttonText = "권한 설정하기",
        buttonColor = "#22C55E" // green-500
    )

    // 3단계: 권한 허용 완료
    object PermissionSuccess : OnboardingPageData(
        icon = "⚡",
        title = "준비 완료!",
        description = "이제 순간의 악상을 빠르게 녹음하고 노래와 악보로 확인해보세요!",
        backgroundColor = "#FEFCE8", // yellow-50
        buttonText = "시작하기",
        buttonColor = "#EAB308" // yellow-500
    )

    /**
     * 문제 의식 -> 권한 요청 -> 권한 허용 완료
     *
     *
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