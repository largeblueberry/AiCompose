package com.largeblueberry.feature_sheetmusic.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.feature_sheetmusic.domain.GenerateSheetMusicUseCase
import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.feature_sheetmusic.domain.repository.SheetMusicRepository // ✅ Repository 직접 주입
import com.largeblueberry.feature_sheetmusic.ui.state.SheetMusicUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SheetMusicViewModel @Inject constructor(
    private val generateSheetMusicUseCase: GenerateSheetMusicUseCase,
    private val sheetMusicRepository: SheetMusicRepository // ✅ Repository를 직접 주입받습니다.
) : ViewModel() {

    private val _uiState = MutableStateFlow<SheetMusicUiState>(SheetMusicUiState.Idle)
    val uiState: StateFlow<SheetMusicUiState> = _uiState.asStateFlow()

    // ✅ 악보 생성 메서드 (변경 없음)
    // UseCase -> RepositoryImpl 내부에서 자동으로 DB 저장이 처리됩니다.
    fun generateSheetMusic(requestBody: Any) {
        viewModelScope.launch {
            _uiState.value = SheetMusicUiState.Loading
            Log.d("SheetMusicVM", "API 요청 시작: $requestBody")

            generateSheetMusicUseCase(requestBody)
                .onSuccess { sheetMusic ->
                    Log.d("SheetMusicVM", "✅ API 요청 및 DB 저장 성공: $sheetMusic")
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

    // ✅ 업로드된 파일 처리 메서드 (수정됨)
    fun loadUploadedFiles(midiUrl: String, scoreUrl: String) {
        viewModelScope.launch {
            _uiState.value = SheetMusicUiState.Loading
            Log.d("SheetMusicVM", "🔍 받은 MIDI URL: $midiUrl, Score URL: $scoreUrl")

            try {
                // 1. 화면에 표시할 SheetMusic 객체 생성
                val sheetMusic = createSheetMusicFromUrls(midiUrl, scoreUrl)

                // 2. Repository를 통해 DB에 저장
                sheetMusicRepository.saveSheetMusic(sheetMusic)
                    .onSuccess {
                        Log.d("SheetMusicVM", "✅ 업로드된 파일 정보 DB 저장 완료.")
                    }
                    .onFailure {
                        Log.e("SheetMusicVM", "🔴 업로드된 파일 정보 DB 저장 실패.", it)
                    }

                // 3. UI 상태 업데이트
                _uiState.value = SheetMusicUiState.Success(sheetMusic)
                Log.d("SheetMusicVM", "✅ 파일 로드 및 처리 완료.")

            } catch (e: Exception) {
                Log.e("SheetMusicVM", "🔴 파일 로드 실패: ${e.message}", e)
                _uiState.value = SheetMusicUiState.Error("파일을 불러올 수 없습니다: ${e.message}")
            }
        }
    }

    // SheetMusic 객체 생성 헬퍼 함수 (변경 없음)
    private fun createSheetMusicFromUrls(midiUrl: String, scoreUrl: String): SheetMusic {
        return SheetMusic(
            id = "uploaded_${System.currentTimeMillis()}",
            title = "업로드된 악보",
            composer = "Unknown",
            scoreUrl = scoreUrl,
            midiUrl = midiUrl,
            createdAt = getCurrentTimeString(),
            duration = null,
            key = null,
            tempo = null
        )
    }

    // 시간 변환 헬퍼 함수 (변경 없음)
    private fun getCurrentTimeString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun resetState() {
        _uiState.value = SheetMusicUiState.Idle
    }
}
