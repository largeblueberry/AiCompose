package com.largeblueberry.feature_sheetmusic.ui.screen

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 언제 무엇을 JS로 보낼지(검증 대상)를 실제 객체로 본다. WebView는 끼지 않는다.
 * Robolectric은 org.json(안드로이드 구현)을 진짜로 돌리려고만 쓴다.
 */
@RunWith(RobolectricTestRunner::class)
class ScoreRenderStateTest {

    private val state = ScoreRenderState()

    @Test
    fun `페이지 로드 전 XML은 보관했다가 로드되면 한 번 그린다`() {
        assertNull(state.request("a"))

        assertEquals(renderScoreScript("a"), state.onPageLoaded())
    }

    @Test
    fun `로드 전에 여러 번 바뀌면 마지막 것만 그린다`() {
        state.request("a")
        state.request("b")

        assertEquals(renderScoreScript("b"), state.onPageLoaded())
    }

    @Test
    fun `보관한 XML이 없으면 로드되어도 보낼 것이 없다`() {
        assertNull(state.onPageLoaded())
    }

    @Test
    fun `로드 뒤에는 바뀔 때만 다시 그린다`() {
        state.request("a")
        state.onPageLoaded()

        assertNull(state.request("a"))
        assertEquals(renderScoreScript("b"), state.request("b"))
        assertNull(state.request("b"))
    }

    @Test
    fun `따옴표·개행·script 닫는 태그가 있어도 원래 문자열로 되돌아온다`() {
        val xml = "<a title=\"x\" v='y'>\n\\</script></a>"

        val script = renderScoreScript(xml)

        assertFalse(script.contains("</script>"))
        val literal = script.removePrefix("window.renderScore(").removeSuffix(")")
        assertEquals(xml, JSONArray("[$literal]").getString(0))
    }
}
