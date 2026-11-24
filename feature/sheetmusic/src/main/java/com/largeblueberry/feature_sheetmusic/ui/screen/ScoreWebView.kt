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
    val render = remember { ScoreRenderState() }

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

                    addJavascriptInterface(
                        ScoreBridge(
                            onHeight = { height -> post { if (height > 0) contentHeight = height } },
                            onError = { message -> post { currentOnError(message) } },
                        ),
                        BRIDGE_NAME,
                    )

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest,
                        ): WebResourceResponse? = loader.shouldInterceptRequest(request.url)

                        override fun onPageFinished(view: WebView, url: String?) {
                            render.onPageLoaded()?.let { view.evaluateJavascript(it, null) }
                        }
                    }
                    loadUrl(PAGE_URL)
                }
            },
            update = { view ->
                // 처음에는 페이지가 로드되기를 기다렸다 그리고, 그 뒤에는 바뀔 때만 다시 그린다
                render.request(musicXml)?.let { view.evaluateJavascript(it, null) }
            },
            onRelease = { view -> view.destroy() },
        )
        if (contentHeight == 0) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

/**
 * WebView 하나에 붙은 렌더링 상태. 언제 무엇을 JS로 보낼지만 정하고 WebView는 모른다.
 * 재구성 사이에 유지되지만 화면에 그리는 값은 아니다.
 */
internal class ScoreRenderState {
    private var pageLoaded = false
    private var pending: String? = null
    private var drawn: String? = null

    /** 화면이 XML을 넘길 때. 지금 실행할 스크립트, 없으면 null(페이지 로드 전이면 보관한다). */
    fun request(xml: String): String? =
        if (pageLoaded) {
            script(xml)
        } else {
            pending = xml
            null
        }

    /** 페이지 로드가 끝났을 때. 보관해 둔 XML이 있으면 그 스크립트. */
    fun onPageLoaded(): String? {
        pageLoaded = true
        return pending?.let(::script)
    }

    private fun script(xml: String): String? {
        pending = null
        if (xml == drawn) return null
        drawn = xml
        return renderScoreScript(xml)
    }
}

/** XML을 JS 문자열 리터럴로 감싸 넘긴다. 따옴표·개행·`</script>`가 있어도 안전하다. */
internal fun renderScoreScript(xml: String): String = "window.renderScore(${JSONObject.quote(xml)})"

/** OSMD 페이지(index.html)가 부르는 창구. JS 스레드에서 불리므로 화면 반영은 받는 쪽이 메인으로 넘긴다. */
internal class ScoreBridge(
    private val onHeight: (Int) -> Unit,
    private val onError: (String) -> Unit,
) {
    @JavascriptInterface
    fun reportHeight(height: Int) = onHeight(height)

    @JavascriptInterface
    fun reportError(message: String) {
        Log.e(TAG, "OSMD error: $message")
        onError(message)
    }
}

internal const val BRIDGE_NAME = "AndroidBridge"
internal const val PAGE_URL = "https://${WebViewAssetLoader.DEFAULT_DOMAIN}/assets/osmd/index.html"
internal const val MIN_HEIGHT_DP = 200
private const val TAG = "ScoreWebView"
