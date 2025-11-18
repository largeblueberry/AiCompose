package com.largeblueberry.feature_sheetmusic.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.feature_sheetmusic.domain.GenerateSheetMusicUseCase
import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.feature_sheetmusic.ui.state.SheetMusicUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SheetMusicViewModel @Inject constructor(
    private val generateSheetMusicUseCase: GenerateSheetMusicUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SheetMusicUiState>(SheetMusicUiState.Idle)
    val uiState: StateFlow<SheetMusicUiState> = _uiState.asStateFlow()

    // 기존 악보 생성 메서드
    fun generateSheetMusic(requestBody: Any) {
        viewModelScope.launch {
            _uiState.value = SheetMusicUiState.Loading

            Log.d("SheetMusicVM", "요청 시작: $requestBody")

            generateSheetMusicUseCase(requestBody)
                .onSuccess { sheetMusic ->
                    Log.d("SheetMusicVM", "성공: $sheetMusic")
                    _uiState.value = SheetMusicUiState.Success(sheetMusic)
                }
                .onFailure { exception ->
                    Log.e("SheetMusicVM", "실패: ${exception.message}", exception)
                    _uiState.value = SheetMusicUiState.Error(
                        exception.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                }
        }
    }

    // 🔥 새로 추가: 업로드된 파일 처리 (두 개 URL)
    fun loadUploadedFiles(midiUrl: String, scoreUrl: String) {
        viewModelScope.launch {
            _uiState.value = SheetMusicUiState.Loading

            // 🔍 URL 디버깅
            Log.d("SheetMusicVM", "🔍 받은 MIDI URL: $midiUrl")
            Log.d("SheetMusicVM", "🔍 받은 Score URL: $scoreUrl")

            // URL 유효성 검사
            if (midiUrl.startsWith("http")) {
                Log.d("SheetMusicVM", "✅ MIDI URL은 절대 경로")
            } else {
                Log.w("SheetMusicVM", "⚠️ MIDI URL은 상대 경로: $midiUrl")
            }

            if (scoreUrl.startsWith("http")) {
                Log.d("SheetMusicVM", "✅ Score URL은 절대 경로")
            } else {
                Log.w("SheetMusicVM", "⚠️ Score URL은 상대 경로: $scoreUrl")
            }

            try {
                // 🕐 파일 생성 완료 대기 (서버에서 변환 시간 필요)
                Log.d("SheetMusicVM", "⏳ 파일 생성 완료 대기 중...")
                delay(3000) // 3초 대기

                // ✅ 실제 SheetMusic 객체 생성
                val sheetMusic = createSheetMusicFromUrls(midiUrl, scoreUrl)

                Log.d("SheetMusicVM", "✅ 파일 로드 성공!")
                _uiState.value = SheetMusicUiState.Success(sheetMusic)

            } catch (e: Exception) {
                Log.e("SheetMusicVM", "🔴 파일 로드 실패: ${e.message}", e)
                _uiState.value = SheetMusicUiState.Error(
                    "파일을 불러올 수 없습니다: ${e.message}"
                )
            }
        }
    }

    // 🔥 단일 URL로 파일 로드
    fun loadUploadedFile(url: String) {
        viewModelScope.launch {
            _uiState.value = SheetMusicUiState.Loading

            Log.d("SheetMusicVM", "🔍 받은 URL: $url")

            try {
                // ⏳ 파일 생성 완료 대기
                Log.d("SheetMusicVM", "⏳ 파일 생성 완료 대기 중...")
                delay(3000) // 3초 대기

                // ✅ 실제 SheetMusic 객체 생성
                val sheetMusic = createSheetMusicFromUrl(url)

                Log.d("SheetMusicVM", "✅ 파일 로드 성공!")

                // 🔍 받은 데이터 상세 로그
                Log.d("SheetMusicVM", "📋 SheetMusic 데이터 상세:")
                Log.d("SheetMusicVM", "  - id: ${sheetMusic.id}")
                Log.d("SheetMusicVM", "  - title: ${sheetMusic.title}")
                Log.d("SheetMusicVM", "  - composer: ${sheetMusic.composer}")
                Log.d("SheetMusicVM", "  - scoreUrl: '${sheetMusic.scoreUrl}' (길이: ${sheetMusic.scoreUrl?.length ?: 0})")
                Log.d("SheetMusicVM", "  - midiUrl: '${sheetMusic.midiUrl}' (길이: ${sheetMusic.midiUrl?.length ?: 0})")
                Log.d("SheetMusicVM", "  - createdAt: ${sheetMusic.createdAt}")

                // 🔍 scoreUrl이 비어있는지 확인
                if (sheetMusic.scoreUrl.isNullOrEmpty()) {
                    Log.w("SheetMusicVM", "⚠️ scoreUrl이 비어있습니다!")
                } else {
                    Log.d("SheetMusicVM", "✅ scoreUrl이 존재합니다: ${sheetMusic.scoreUrl}")
                }

                _uiState.value = SheetMusicUiState.Success(sheetMusic)

            } catch (e: Exception) {
                Log.e("SheetMusicVM", "🔴 파일 로드 실패: ${e.message}", e)
                _uiState.value = SheetMusicUiState.Error(
                    "파일을 불러올 수 없습니다: ${e.message}"
                )
            }
        }
    }

    // 🔥 두 개 URL에서 SheetMusic 객체 생성
    private fun createSheetMusicFromUrls(midiUrl: String, scoreUrl: String): SheetMusic {
        val currentTime = getCurrentTimeString()

        return SheetMusic(
            id = "uploaded_${System.currentTimeMillis()}",
            title = "업로드된 악보",
            composer = "Unknown",
            scoreUrl = scoreUrl,
            midiUrl = midiUrl,
            createdAt = currentTime,
            duration = null,
            key = null,
            tempo = null
        )
    }

    // 🔥 단일 URL에서 SheetMusic 객체 생성
    private fun createSheetMusicFromUrl(url: String): SheetMusic {
        val isMidi = url.contains("midi", ignoreCase = true) ||
                url.endsWith(".mid", ignoreCase = true) ||
                url.endsWith(".midi", ignoreCase = true)

        val currentTime = getCurrentTimeString()

        return SheetMusic(
            id = "uploaded_${System.currentTimeMillis()}",
            title = "업로드된 ${if (isMidi) "MIDI 파일" else "악보"}",
            composer = "Unknown",
            scoreUrl = if (!isMidi) url else null,
            midiUrl = if (isMidi) url else null,
            createdAt = currentTime,
            duration = null,
            key = null,
            tempo = null
        )
    }

    // 🔥 현재 시간을 String으로 변환하는 헬퍼 메서드
    private fun getCurrentTimeString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun resetState() {
        _uiState.value = SheetMusicUiState.Idle
    }
}