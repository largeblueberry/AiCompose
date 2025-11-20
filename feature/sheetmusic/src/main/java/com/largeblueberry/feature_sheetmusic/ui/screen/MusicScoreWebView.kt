package com.largeblueberry.feature_sheetmusic.ui.screen

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MusicScoreWebView(
    modifier: Modifier = Modifier,
    htmlContentFromServer: String
) {
    val logTag = "MusicScoreWebView"

    // 웹뷰의 콘텐츠 높이를 저장할 상태 변수 (단위: px)
    var webViewHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    // px 단위를 Dp 단위로 변환합니다.
    val webViewHeightDp = with(density) { webViewHeight.toDp() }

    // JavaScript에서 호출할 수 있는 인터페이스 클래스
    class WebAppInterface(private val onHeightReady: (Int) -> Unit) {
        @JavascriptInterface
        fun reportContentHeight(height: Int) {
            Log.d("WebAppInterface", "New height reported from JS: $height px")
            // 전달받은 높이로 상태를 업데이트합니다.
            onHeightReady(height)
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            // 콘텐츠 높이가 측정되면 해당 높이를 적용하고, 아니면 기본 높이(0.dp)를 유지합니다.
            // 부모 컴포저블이 높이를 결정하도록 합니다.
            .then(if (webViewHeightDp > 0.dp) Modifier.height(webViewHeightDp) else Modifier),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.loadsImagesAutomatically = true
                settings.blockNetworkImage = false

                // 확대/축소 기능 (필요시 사용)
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false

                // 페이지가 화면 크기에 맞게 보이도록 설정
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                // 레이아웃이 불안정하게 변경되는 것을 방지하기 위해 스크롤바를 숨깁니다.
                isVerticalScrollBarEnabled = false

                // JavaScript 인터페이스 추가
                addJavascriptInterface(
                    WebAppInterface { newHeight ->
                        // UI 스레드에서 상태를 안전하게 업데이트합니다.
                        if (webViewHeight != newHeight) {
                            webViewHeight = newHeight
                        }
                    },
                    "AndroidBridge" // JS에서 호출할 이름
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d(logTag, "✅ WebView 페이지 로딩 완료: $url")
                        // 페이지 로딩이 끝나면, 콘텐츠 높이를 측정하고 안드로이드로 전달하는 JS를 주입합니다.
                        val jsCode = """
                            javascript:(function() {
                                function reportHeight() {
                                    const height = document.body.scrollHeight;
                                    if (window.AndroidBridge) {
                                        window.AndroidBridge.reportContentHeight(height);
                                    }
                                }
                                // ResizeObserver를 사용해 콘텐츠 크기 변경을 감지하고 높이를 다시 보고합니다.
                                const observer = new ResizeObserver(function() { reportHeight(); });
                                observer.observe(document.body);
                                // 초기 높이 보고
                                reportHeight();
                            })();
                        """.trimIndent()
                        view?.loadUrl(jsCode)
                        Log.d(logTag, "✅ Height reporting JS injected.")
                    }
                }
            }
        },
        update = { webView ->
            Log.d(logTag, "🚀 WebView 업데이트 및 HTML 데이터 로딩 시작")
            // 새로운 콘텐츠가 로드될 때 높이를 초기화합니다.
            webViewHeight = 0
            webView.loadDataWithBaseURL(
                "https://teamproject.p-e.kr/",
                htmlContentFromServer,
                "text/html",
                "UTF-8",
                null
            )
            Log.d(logTag, "✅ loadDataWithBaseURL 호출 완료")
        }
    )
}
