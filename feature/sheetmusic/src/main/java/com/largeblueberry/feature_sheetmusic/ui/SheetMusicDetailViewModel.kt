package com.largeblueberry.feature_sheetmusic.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.analyticshelper.AnalyticsHelper
import com.largeblueberry.feature_sheetmusic.domain.GetSheetMusicDetailUseCase
import com.largeblueberry.feature_sheetmusic.ui.util.SheetMusicDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SheetMusicDetailViewModel @Inject constructor(
    private val getSheetMusicDetailUseCase: GetSheetMusicDetailUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    companion object {
        private const val TAG = "SheetMusicDetailVM"
    }

    private val _uiState = MutableStateFlow(SheetMusicDetailUiState())
    val uiState: StateFlow<SheetMusicDetailUiState> = _uiState.asStateFlow()

    /**
     * 악보 상세 정보 로드
     */
    fun loadSheetMusic(sheetMusicId: String?) {
        Log.d(TAG, "🔍 loadSheetMusic called with ID: $sheetMusicId")

        if (sheetMusicId.isNullOrBlank()) {
            Log.e(TAG, "❌ Invalid sheet music ID: $sheetMusicId")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "잘못된 악보 ID입니다."
            )
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "🔄 Starting to load sheet music...")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                analyticsHelper.logEvent("sheet_music_detail_load_start",
                    mapOf("sheet_music_id" to sheetMusicId))

                val result = getSheetMusicDetailUseCase(sheetMusicId)

                result.fold(
                    onSuccess = { sheetMusic ->
                        Log.d(TAG, "✅ Sheet music loaded successfully")
                        Log.d(TAG, "📄 Title: ${sheetMusic.title}")
                        Log.d(TAG, "🎼 Has Score: ${sheetMusic.hasScore}")
                        Log.d(TAG, "🎵 Has MIDI: ${sheetMusic.hasMidi}")

                        analyticsHelper.logEvent("sheet_music_detail_load_success",
                            mapOf(
                                "sheet_music_id" to sheetMusicId,
                                "has_score" to sheetMusic.hasScore.toString(),
                                "has_midi" to sheetMusic.hasMidi.toString()
                            ))

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            sheetMusic = sheetMusic,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "❌ Error loading sheet music", exception)
                        analyticsHelper.logEvent("sheet_music_detail_load_failure",
                            mapOf(
                                "sheet_music_id" to sheetMusicId,
                                "error" to (exception.message ?: "Unknown error")
                            ))

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "악보 로딩 중 오류가 발생했습니다."
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error loading sheet music", e)
                analyticsHelper.logEvent("sheet_music_detail_load_exception",
                    mapOf(
                        "sheet_music_id" to sheetMusicId,
                        "error" to (e.message ?: "Unknown error")
                    ))

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "알 수 없는 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }

    /**
     * 재시도
     */
    fun retry(sheetMusicId: String?) {
        Log.d(TAG, "🔄 Retrying with sheet music ID: $sheetMusicId")
        analyticsHelper.logEvent("sheet_music_detail_retry",
            mapOf("sheet_music_id" to (sheetMusicId ?: "null")))
        loadSheetMusic(sheetMusicId)
    }

    /**
     * 에러 상태 클리어
     */
    fun clearError() {
        Log.d(TAG, "🧹 Clearing error state")
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * MIDI 재생 버튼 클릭 이벤트
     */
    fun onMidiPlayClicked() {
        val sheetMusic = _uiState.value.sheetMusic
        if (sheetMusic?.hasMidi == true) {
            Log.d(TAG, "🎵 MIDI play clicked for: ${sheetMusic.title}")
            analyticsHelper.logEvent("sheet_music_midi_play_clicked",
                mapOf("sheet_music_id" to sheetMusic.id))
        }
    }

    /**
     * 악보 보기 버튼 클릭 이벤트
     */
    fun onScoreViewClicked() {
        val sheetMusic = _uiState.value.sheetMusic
        if (sheetMusic?.hasScore == true) {
            Log.d(TAG, "🎼 Score view clicked for: ${sheetMusic.title}")
            analyticsHelper.logEvent("sheet_music_score_view_clicked",
                mapOf("sheet_music_id" to sheetMusic.id))
        }
    }
}