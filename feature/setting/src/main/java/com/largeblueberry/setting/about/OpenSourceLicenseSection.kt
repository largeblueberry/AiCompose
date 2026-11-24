package com.largeblueberry.setting.about

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.largeblueberry.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** About 화면 맨 아래의 오픈소스 고지. 항목을 누르면 라이선스 원문(과 NOTICE)이 펼쳐진다. */
@Composable
internal fun OpenSourceLicenseSection(
    licenses: List<OpenSourceLicense> = OpenSourceLicenses.all,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        licenses.forEach { license ->
            OpenSourceLicenseItem(license)
            HorizontalDivider()
        }
    }
}

@Composable
private fun OpenSourceLicenseItem(license: OpenSourceLicense) {
    var expanded by rememberSaveable(license.name) { mutableStateOf(false) }
    val toggleLabel = stringResource(if (expanded) R.string.ossHideLicense else R.string.ossShowLicense)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClickLabel = toggleLabel) { expanded = !expanded }
            .padding(vertical = 12.dp)
    ) {
        Text(text = license.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Text(
            text = "${license.copyright}\n${license.licenseName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (expanded) {
            val text by licenseText(license)
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/** 펼쳤을 때만 assets에서 읽는다(Apache 원문은 11KB). 읽기는 메인 스레드 밖에서. */
@Composable
private fun licenseText(license: OpenSourceLicense) = LocalContext.current.let { context ->
    produceState(initialValue = "", license) {
        value = withContext(Dispatchers.IO) { readLicense(context, license) }
    }
}

/**
 * 저작권 문구 + 원문 + (있으면) NOTICE.
 * 공용 원문(MIT·Apache)에는 저작권 줄이 없어서 앞에 붙인다. OSMD 원문처럼 이미 들어 있으면 다시 붙이지 않는다.
 */
internal fun readLicense(context: Context, license: OpenSourceLicense): String {
    fun asset(path: String) = context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    val body = asset(license.licenseAsset).trim()
    return buildString {
        if (!body.contains(license.copyright)) append(license.copyright).append("\n\n")
        append(body)
        license.noticeAsset?.let { append("\n\n— NOTICE —\n\n").append(asset(it).trim()) }
    }
}
