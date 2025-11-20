package com.largeblueberry.feature_sheetmusic.ui.screen

// MusicScoreWebView 함수 전체를 이 코드로 교체하세요.

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.util.Log

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MusicScoreWebView(
    modifier: Modifier = Modifier,
    // 파라미터 이름을 명확하게 변경합니다. 서버에서 받은 HTML 전체를 받습니다.
    htmlContentFromServer: String
) {
    val logTag = "MusicScoreWebView"

    // 안드로이드의 WebView를 컴포즈에서 사용하기 위한 래퍼
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                // *** 가장 중요한 설정: JavaScript를 반드시 활성화해야 합니다. ***
                settings.javaScriptEnabled = true

                // 외부 리소스(JS, MusicXML)를 로드하기 위해 네트워크 접근을 허용해야 합니다.
                settings.domStorageEnabled = true
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.loadsImagesAutomatically = true
                settings.blockNetworkImage = false // 네트워크를 통해 이미지/리소스를 로드하는 것을 허용

                // 확대/축소 기능
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false

                // 페이지가 화면 크기에 맞게 보이도록 설정
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                // 웹뷰 클라이언트 설정 (페이지 로딩 상태 등을 로그로 확인하기 위함)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d(logTag, "✅ WebView 페이지 로딩 완료: $url")
                    }
                }
            }
        },
        update = { webView ->
            Log.d(logTag, "🚀 WebView 업데이트 및 HTML 데이터 로딩 시작")

            // 서버에서 받은 HTML 데이터를 그대로 로드합니다.
            // BaseURL을 설정해 주면 상대 경로 리소스 로드에 도움이 될 수 있습니다.
            webView.loadDataWithBaseURL(
                "https://teamproject.p-e.kr/", // HTML 내의 상대 경로 리소스를 위한 기준 URL
                htmlContentFromServer,        // 서버에서 받은 HTML 문자열
                "text/html",                  // 데이터 타입
                "UTF-8",                      // 인코딩
                null                          // History URL
            )
            Log.d(logTag, "✅ loadDataWithBaseURL 호출 완료")
        }
    )
}