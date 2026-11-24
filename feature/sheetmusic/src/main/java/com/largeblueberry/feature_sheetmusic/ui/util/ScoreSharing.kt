package com.largeblueberry.feature_sheetmusic.ui.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * 악보 파일을 다른 앱(메신저, DAW, 악보 앱)으로 보낸다.
 * 앱 내부 파일이라 FileProvider content:// URI로 읽기 권한만 잠깐 준다.
 * authority는 app 매니페스트의 provider와 같아야 한다(applicationId + ".fileprovider").
 */
internal object ScoreSharing {
    const val MIME_MIDI = "audio/midi"
    const val MIME_MUSIC_XML = "application/vnd.recordare.musicxml+xml"

    fun share(context: Context, path: String, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(path))
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            // 일부 앱은 EXTRA_STREAM만으로는 권한을 못 받아 ClipData도 같이 넣는다
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, chooserTitle))
    }
}
