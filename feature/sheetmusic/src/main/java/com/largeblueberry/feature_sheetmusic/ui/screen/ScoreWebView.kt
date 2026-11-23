package com.largeblueberry.feature_sheetmusic.ui.screen

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import org.json.JSONObject

/**
 * MusicXML을 앱 assets의 OpenSheetMusicDisplay(osmd/index.html)로 그린다. 네트워크를 쓰지 않는다.
 *
 * - 페이지는 WebViewAssetLoader의 가상 https 주소로 열고 file·content 접근은 끈다(docs/troubleshooting.md C-7).
 * - WebView 높이를 악보 높이에 맞춘다(바깥 화면이 스크롤). 그래서 핀치 줌은 끈다: 확대하면 OSMD가 폭에 맞춰
 *   다시 그리면서 높이가 계속 바뀌어 화면이 튄다.
 * - 페이지가 로드된 뒤 musicXml이 바뀌면 다시 그린다.
 * - 컴포지션에서 빠질 때 WebView를 destroy한다(안 하면 화면을 열고 닫을 때마다 WebView가 남는다).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ScoreWebView(
    musicXml: String,
    onRenderError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // CSS px. viewport가 device-width라 1 CSS px = 1 dp
    var contentHeight by remember { mutableIntStateOf(0) }
    val currentOnError by rememberUpdatedState(onRenderError)
    val render = remember { RenderState() }

    Box(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (contentHeight > 0) contentHeight.dp else MIN_HEIGHT_DP.dp),
            factory = { context ->
                val loader = WebViewAssetLoader.Builder()
                    .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                    .build()
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.setSupportZoom(false)
                    settings.builtInZoomControls = false
                    isVerticalScrollBarEnabled = false

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun reportHeight(height: Int) {
                            post { if (height > 0) contentHeight = height }
                        }

                        @JavascriptInterface
                        fun reportError(message: String) {
                            Log.e(TAG, "OSMD error: $message")
                            post { currentOnError(message) }
                        }
                    }, "AndroidBridge")

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest,
                        ): WebResourceResponse? = loader.shouldInterceptRequest(request.url)

                        override fun onPageFinished(view: WebView, url: String?) {
                            render.pageLoaded = true
                            render.pending?.let { render.draw(view, it) }
                        }
                    }
                    loadUrl(PAGE_URL)
                }
            },
            update = { view ->
                // 처음에는 페이지가 로드되기를 기다렸다 그리고, 그 뒤에는 바뀔 때만 다시 그린다
                if (render.pageLoaded) render.draw(view, musicXml) else render.pending = musicXml
            },
            onRelease = { view -> view.destroy() },
        )
        if (contentHeight == 0) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

/** WebView 하나에 붙은 렌더링 상태. 재구성 사이에 유지되지만 화면에 그리는 값은 아니다. */
private class RenderState {
    var pageLoaded = false
    var pending: String? = null
    private var drawn: String? = null

    fun draw(view: WebView, xml: String) {
        pending = null
        if (xml == drawn) return
        drawn = xml
        view.evaluateJavascript("window.renderScore(${JSONObject.quote(xml)})", null)
    }
}

private const val TAG = "ScoreWebView"
private const val MIN_HEIGHT_DP = 200
private const val PAGE_URL = "https://${WebViewAssetLoader.DEFAULT_DOMAIN}/assets/osmd/index.html"
