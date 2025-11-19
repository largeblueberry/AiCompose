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

    // 기존 악보 생성 메서드 (API 직접 호출)
    fun generateSheetMusic(requestBody: Any) {
        viewModelScope.launch {
            _uiState.value = SheetMusicUiState.Loading
            Log.d("SheetMusicVM", "API 요청 시작: $requestBody")

            generateSheetMusicUseCase(requestBody)
                .onSuccess { sheetMusic ->
                    // UseCase가 반환한 SheetMusic 객체는 이미 midiUrl과 scoreUrl을 모두 가짐
                    Log.d("SheetMusicVM", "✅ API 요청 성공: $sheetMusic")
                    _uiState.value = SheetMusicUiState.Success(sheetMusic)
                }
                .onFailure { exception ->
                    Log.e("SheetMusicVM", "🔴 API 요청 실패: ${exception.message}", exception)
                    _uiState.value = SheetMusicUiState.Error(
                        exception.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                }
        }
    }

    // ✅ 업로드된 파일 처리 (두 URL을 받는 유일한 메서드)
    fun loadUploadedFiles(midiUrl: String, scoreUrl: String) {
        viewModelScope.launch {
            _uiState.value = SheetMusicUiState.Loading

            Log.d("SheetMusicVM", "🔍 받은 MIDI URL: $midiUrl")
            Log.d("SheetMusicVM", "🔍 받은 Score URL: $scoreUrl")

            try {
                // 서버에서 파일 변환 및 저장에 시간이 걸릴 수 있으므로 잠시 대기
                Log.d("SheetMusicVM", "⏳ 파일 생성 대기 중...")
                delay(2000) // 2초 대기 (네트워크 상태에 따라 조절 가능)

                // 두 URL을 모두 사용하여 SheetMusic 객체 생성
                val sheetMusic = createSheetMusicFromUrls(midiUrl, scoreUrl)

                Log.d("SheetMusicVM", "✅ 파일 로드 성공! SheetMusic 객체 생성 완료.")
                Log.d("SheetMusicVM", "  - scoreUrl: ${sheetMusic.scoreUrl}")
                Log.d("SheetMusicVM", "  - midiUrl: ${sheetMusic.midiUrl}")

                _uiState.value = SheetMusicUiState.Success(sheetMusic)

            } catch (e: Exception) {
                Log.e("SheetMusicVM", "🔴 파일 로드 실패: ${e.message}", e)
                _uiState.value = SheetMusicUiState.Error(
                    "파일을 불러올 수 없습니다: ${e.message}"
                )
            }
        }
    }

    // ✅ 두 URL로부터 SheetMusic 객체를 생성하는 헬퍼 함수
    private fun createSheetMusicFromUrls(midiUrl: String, scoreUrl: String): SheetMusic {
        val currentTime = getCurrentTimeString()

        return SheetMusic(
            id = "uploaded_${System.currentTimeMillis()}",
            title = "업로드된 악보",
            composer = "Unknown",
            scoreUrl = scoreUrl, // 전달받은 scoreUrl 사용
            midiUrl = midiUrl,   // 전달받은 midiUrl 사용
            createdAt = currentTime,
            duration = null,
            key = null,
            tempo = null
        )
    }

    // 현재 시간을 문자열로 변환하는 헬퍼 메서드
    private fun getCurrentTimeString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun resetState() {
        _uiState.value = SheetMusicUiState.Idle
    }
}