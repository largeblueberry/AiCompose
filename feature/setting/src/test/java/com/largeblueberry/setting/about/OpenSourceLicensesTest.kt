package com.largeblueberry.setting.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 오픈소스 고지 목록이 실제로 보여 줄 수 있는 상태인지. 목록은 손으로 관리하므로(docs/troubleshooting.md C-11)
 * 원문 파일 경로가 틀리거나 저작권이 비는 실수를 여기서 막는다. 단위 테스트의 작업 폴더는 모듈 폴더다.
 */
class OpenSourceLicensesTest {

    private fun asset(path: String) = File("src/main/assets/$path")

    @Test
    fun `모든 항목의 라이선스 원문과 NOTICE 파일이 assets에 있고 비어 있지 않다`() {
        OpenSourceLicenses.all.forEach { license ->
            listOfNotNull(license.licenseAsset, license.noticeAsset).forEach { path ->
                val file = asset(path)
                assertTrue("${license.name}: $path 없음", file.isFile)
                assertTrue("${license.name}: $path 비어 있음", file.readText().isNotBlank())
            }
        }
    }

    @Test
    fun `모든 항목에 저작권 문구가 있고 이름이 겹치지 않는다`() {
        val all = OpenSourceLicenses.all
        all.forEach { assertTrue("${it.name}: 저작권 없음", it.copyright.startsWith("Copyright")) }
        assertEquals(all.size, all.map { it.name }.toSet().size)
    }

    @Test
    fun `공용 원문에는 저작권 줄이 없고, OSMD 원문에는 OSMD 저작권이 들어 있다`() {
        // 공용 원문(MIT·Apache)은 화면에서 항목의 저작권을 앞에 붙인다. 원문에 다른 저작권이 섞여 있으면 틀린 고지가 된다.
        listOf(OpenSourceLicenses.MIT, OpenSourceLicenses.APACHE_2_0).forEach { path ->
            val firstLine = asset(path).readLines().first { it.isNotBlank() }
            assertTrue("$path 첫 줄에 저작권이 있다: $firstLine", !firstLine.trim().startsWith("Copyright"))
        }
        val osmd = OpenSourceLicenses.all.single { it.licenseAsset == OpenSourceLicenses.OSMD_BSD_3 }
        assertTrue(asset(OpenSourceLicenses.OSMD_BSD_3).readText().contains(osmd.copyright))
    }

    @Test
    fun `Basic Pitch는 Apache NOTICE를 함께 보인다`() {
        val basicPitch = OpenSourceLicenses.all.single { it.name.startsWith("Basic Pitch") }
        assertEquals(OpenSourceLicenses.BASIC_PITCH_NOTICE, basicPitch.noticeAsset)
    }
}
