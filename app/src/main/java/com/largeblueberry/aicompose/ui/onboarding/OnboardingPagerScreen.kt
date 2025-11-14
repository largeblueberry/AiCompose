package com.largeblueberry.aicompose.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

@Composable
fun OnboardingPagerScreen(
    onPermissionRequest: () -> Unit = {},
    onComplete: () -> Unit
) {
    // 동적으로 페이지 수 계산
    val pageCount by remember { derivedStateOf { OnboardingPageData.getPageCount() } }
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Page Indicator
        PageIndicator(
            currentPage = pagerState.currentPage,
            pageCount = pageCount,
            modifier = Modifier.padding(top = 40.dp)
        )

        // Content Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(pageData = OnboardingPageData.getPageData(page))
        }

        // Bottom Buttons - 개선된 로직
        when {
            // 권한 요청 페이지 (index 1)에서는 2개 버튼
            OnboardingPageData.isPermissionRequestPage(pagerState.currentPage) -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 권한 설정 버튼 (메인)
                    Button(
                        onClick = {
                            onPermissionRequest()
                            scope.launch {
                                pagerState.animateScrollToPage(2) // PermissionSuccess로
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "권한 설정하러 가기",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 나머지 페이지에서는 1개 버튼
            else -> {
                val currentPageData = OnboardingPageData.getPageData(pagerState.currentPage)

                Button(
                    onClick = {
                        if (OnboardingPageData.isLastPage(pagerState.currentPage)) {
                            onComplete()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(currentPageData.buttonColor.toColorInt())
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = currentPageData.buttonText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}