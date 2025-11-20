package com.largeblueberry.feature_sheetmusic.ui.screen


import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.largeblueberry.feature_sheetmusic.ui.ErrorContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun RemoteScoreViewer(
    modifier: Modifier = Modifier,
    // 이 URL은 이제 이미지가 아닌 HTML을 반환하는 서버의 엔드포인트여야 합니다.
    scoreUrl: String
) {
    val logTag = "RemoteScoreViewer"

    // 1. 상태 변수 이름 변경: imageAsBase64 -> htmlContent
    var htmlContent by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = scoreUrl, key2 = retryTrigger) {
        isLoading = true
        hasError = null
        htmlContent = null
        Log.d(logTag, "🚀 HTML 악보 로딩 시작: $scoreUrl")

        // Firebase 토큰 가져오기 (기존 로직과 동일)
        val token: String? = try {
            Firebase.auth.currentUser?.getIdToken(false)?.await()?.token
                ?: throw IllegalStateException("Firebase 인증 토큰이 null입니다.")
        } catch (e: Exception) {
            Log.e(logTag, "🔥 Firebase 토큰 가져오기 실패", e)
            hasError = "사용자 인증 정보를 가져오는 데 실패했습니다."
            isLoading = false
            return@LaunchedEffect
        }
        Log.d(logTag, "✅ Firebase 토큰 가져오기 성공")

        // 네트워크 통신 (IO 스레드에서)
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(scoreUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.connect()

                val responseCode = connection.responseCode
                Log.d(logTag, "📡 HTTP Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 2. 핵심 변경: 이미지 바이트 대신 텍스트(HTML)로 읽어오기
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

    // UI 렌더링 (로딩, 에러, 성공)
    when {
        isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        hasError != null -> {
            ErrorContent( // 기존에 사용하시던 에러 컴포저블
                message = hasError!!,
                onRetry = { retryTrigger++ }
            )
        }
        // 3. 렌더링 조건 및 전달 데이터 변경
        htmlContent != null -> {
            Log.d(logTag, "🎉 MusicScoreWebView에 HTML 데이터 전달 및 렌더링 시도")
            // 이제 MusicScoreWebView에 HTML 문자열을 직접 전달합니다.
            MusicScoreWebView(
                modifier = modifier,
                htmlContentFromServer = htmlContent!!
            )
        }
    }
}