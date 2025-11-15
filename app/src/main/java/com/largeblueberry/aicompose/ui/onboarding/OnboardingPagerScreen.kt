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
import androidx.compose.ui.res.stringResource // 이 import가 추가되었습니다.

@Composable
fun OnboardingPagerScreen(
    onPermissionRequest: () -> Unit = {},
    onComplete: () -> Unit
) {
    val pageCount by remember { derivedStateOf { OnboardingPageData.getPageCount() } }
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    val currentPageData = OnboardingPageData.getPageData(pagerState.currentPage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        PageIndicator(
            currentPage = pagerState.currentPage,
            pageCount = pageCount,
            modifier = Modifier.padding(top = 40.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(pageData = OnboardingPageData.getPageData(page))
        }

        // Bottom Buttons
        when {
            // 권한 요청 페이지
            OnboardingPageData.isPermissionRequestPage(pagerState.currentPage) -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            onPermissionRequest()
                            scope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentPageData.buttonColor()
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(currentPageData.buttonTextResId),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 나머지 페이지
            else -> {
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
                        containerColor = currentPageData.buttonColor()
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        // 수정: buttonTextResId를 사용하여 stringResource로 변환
                        text = stringResource(currentPageData.buttonTextResId),
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