package com.largeblueberry.feature_sheetmusic.ui.screen

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * ScoreWebView의 연결(검증 대상): 설정, 처음 여는 주소, 브리지 → 화면 상태, 화면에서 빠질 때 정리.
 * WebView 엔진(나머지)은 Robolectric shadow라 JS를 실행하지 않고 호출만 기록한다.
 * 그래서 페이지 로드와 JS의 브리지 호출은 테스트가 흉내 낸다. OSMD가 실제로 그리는지는 범위 밖(docs/device-checklist.md).
 */
@RunWith(RobolectricTestRunner::class)
class ScoreWebViewTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val errors = mutableListOf<String>()

    private fun show(xml: String = XML): WebView {
        // 높이는 Compose 쪽 크기로 본다(Robolectric은 WebView 자체를 배치하지 않아 height가 0이다)
        compose.setContent {
            Box(Modifier.testTag(HOST)) { ScoreWebView(musicXml = xml, onRenderError = { errors += it }) }
        }
        compose.waitForIdle()
        return findWebView(compose.activity.window.decorView)!!
    }

    private fun findWebView(view: View): WebView? = when (view) {
        is WebView -> view
        is ViewGroup -> (0 until view.childCount).firstNotNullOfOrNull { findWebView(view.getChildAt(it)) }
        else -> null
    }

    /** 페이지 로드 완료를 흉내 낸다(실제 엔진이면 WebView가 부른다). */
    private fun WebView.finishLoading() {
        shadowOf(this).webViewClient.onPageFinished(this, PAGE_URL)
        compose.waitForIdle()
    }

    private val WebView.bridge get() = shadowOf(this).getJavascriptInterface(BRIDGE_NAME) as ScoreBridge

    /** 브리지는 결과를 post로 메인에 넘기므로 메인 루퍼를 비우고 재구성을 기다린다. */
    private fun settle() {
        shadowOf(Looper.getMainLooper()).idle()
        compose.waitForIdle()
    }

    @Test
    fun `JS만 켜고 파일·콘텐츠 접근과 줌은 끈다`() {
        val settings = show().settings

        assertTrue(settings.javaScriptEnabled)
        assertFalse(settings.allowFileAccess)
        assertFalse(settings.allowContentAccess)
        assertFalse(settings.supportZoom())
        assertFalse(settings.builtInZoomControls)
    }

    @Test
    fun `앱 assets의 가상 https 주소를 열고 브리지를 등록한다`() {
        val webView = show()

        assertEquals(PAGE_URL, shadowOf(webView).lastLoadedUrl)
        assertTrue(shadowOf(webView).getJavascriptInterface(BRIDGE_NAME) is ScoreBridge)
    }

    @Test
    fun `페이지가 로드되기 전에는 그리지 않고, 로드되면 그린다`() {
        val webView = show()
        assertNull(shadowOf(webView).lastEvaluatedJavascript)

        webView.finishLoading()

        assertEquals(renderScoreScript(XML), shadowOf(webView).lastEvaluatedJavascript)
    }

    @Test
    fun `로드 뒤 musicXml이 바뀌면 다시 그린다`() {
        var xml by mutableStateOf(XML)
        compose.setContent { ScoreWebView(musicXml = xml, onRenderError = {}) }
        compose.waitForIdle()
        val webView = findWebView(compose.activity.window.decorView)!!
        webView.finishLoading()

        xml = "<other/>"
        compose.waitForIdle()

        assertEquals(renderScoreScript("<other/>"), shadowOf(webView).lastEvaluatedJavascript)
    }

    @Test
    fun `처음엔 최소 높이, JS가 높이를 알려 주면 그 높이(dp)가 된다`() {
        val webView = show()
        compose.onNodeWithTag(HOST).assertHeightIsEqualTo(MIN_HEIGHT_DP.dp)

        // Robolectric 기본 화면(320x470dp)보다 작은 값이어야 잘리지 않는다
        webView.bridge.reportHeight(320)
        settle()

        compose.onNodeWithTag(HOST).assertHeightIsEqualTo(320.dp)
    }

    @Test
    fun `높이를 받은 뒤 0 이하가 와도 줄어들지 않는다`() {
        val webView = show()
        webView.bridge.reportHeight(320)
        settle()

        webView.bridge.reportHeight(0)
        settle()

        compose.onNodeWithTag(HOST).assertHeightIsEqualTo(320.dp)
    }

    @Test
    fun `JS가 렌더 오류를 알리면 콜백으로 전한다`() {
        val webView = show()

        webView.bridge.reportError("bad xml")
        settle()

        assertEquals(listOf("bad xml"), errors)
    }

    @Test
    fun `컴포지션에서 빠지면 WebView를 destroy한다`() {
        var visible by mutableStateOf(true)
        compose.setContent { if (visible) ScoreWebView(musicXml = XML, onRenderError = {}) }
        compose.waitForIdle()
        val webView = findWebView(compose.activity.window.decorView)!!
        assertFalse(shadowOf(webView).wasDestroyCalled())

        visible = false
        compose.waitForIdle()

        assertTrue(shadowOf(webView).wasDestroyCalled())
    }

    private companion object {
        const val XML = "<score-partwise/>"
        const val HOST = "host"
    }
}
