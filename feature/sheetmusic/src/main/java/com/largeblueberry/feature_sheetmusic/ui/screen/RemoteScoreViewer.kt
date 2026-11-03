package com.largeblueberry.feature_sheetmusic.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.largeblueberry.feature_sheetmusic.ui.ErrorContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun RemoteScoreViewer(
    modifier: Modifier = Modifier,
    scoreUrl: String
) {
    val logTag = "RemoteScoreViewer"

    var htmlContent by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = scoreUrl, key2 = retryTrigger) {
        isLoading = true
        hasError = null
        htmlContent = null
        Log.d(logTag, "🚀 HTML 악보 로딩 시작: $scoreUrl")

        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(scoreUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.connect()

                val responseCode = connection.responseCode
                Log.d(logTag, "📡 HTTP Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val responseText = inputStream.bufferedReader().use { it.readText() }
                    Log.d(logTag, "✅ HTML 다운로드 성공, 내용 길이: ${responseText.length}")

                    htmlContent = responseText
                } else {
                    throw IOException("HTTP 에러 코드: $responseCode - ${connection.responseMessage}")
                }
            } catch (e: Exception) {
                Log.e(logTag, "🔥 HTML 다운로드 실패", e)
                hasError = "악보를 불러오는 데 실패했습니다. (코드: ${connection?.responseCode ?: "N/A"})"
            } finally {
                connection?.disconnect()
                isLoading = false
                Log.d(logTag, "🏁 HTML 로딩 프로세스 종료")
            }
        }
    }

    when {
        isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        hasError != null -> {
            ErrorContent(
                message = hasError!!,
                onRetry = { retryTrigger++ }
            )
        }
        htmlContent != null -> {
            Log.d(logTag, "🎉 MusicScoreWebView에 HTML 데이터 전달 및 렌더링 시도")
            MusicScoreWebView(
                modifier = modifier,
                htmlContentFromServer = htmlContent!!
            )
        }
    }
}
