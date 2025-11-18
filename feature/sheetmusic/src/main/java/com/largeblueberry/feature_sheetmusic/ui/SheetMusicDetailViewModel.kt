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
import java.io.File
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
                        Log.d(TAG, "👤 Composer: ${sheetMusic.composer}")
                        Log.d(TAG, "📁 File Path: ${sheetMusic.filePath}")
                        Log.d(TAG, "🎼 Has Score: ${hasScoreFile(sheetMusic.filePath)}")
                        Log.d(TAG, "🎵 Has MIDI: ${hasMidiFile(sheetMusic.filePath)}")

                        analyticsHelper.logEvent("sheet_music_detail_load_success",
                            mapOf(
                                "sheet_music_id" to sheetMusicId,
                                "has_score" to hasScoreFile(sheetMusic.filePath).toString(),
                                "has_midi" to hasMidiFile(sheetMusic.filePath).toString(),
                                "composer" to sheetMusic.composer,
                                "difficulty" to (sheetMusic.difficulty ?: "unknown")
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
     * 파일 경로에서 악보 파일 존재 여부 확인
     * PDF, PNG, JPG 등의 악보 이미지 파일 확인
     */
    private fun hasScoreFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            val parentDir = file.parentFile ?: return false

            // 같은 디렉토리에서 악보 관련 파일 찾기
            val scoreExtensions = listOf("pdf", "png", "jpg", "jpeg", "svg")
            val baseName = file.nameWithoutExtension

            parentDir.listFiles()?.any { f ->
                val extension = f.extension.lowercase()
                val fileName = f.nameWithoutExtension

                // 같은 이름이거나 score가 포함된 파일명 + 악보 확장자
                (fileName == baseName || fileName.contains("score", ignoreCase = true)) &&
                        scoreExtensions.contains(extension)
            } ?: false
        } catch (e: Exception) {
            Log.w(TAG, "Error checking score file: ${e.message}")
            false
        }
    }

    /**
     * 파일 경로에서 MIDI 파일 존재 여부 확인
     */
    private fun hasMidiFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            val parentDir = file.parentFile ?: return false

            // 같은 디렉토리에서 MIDI 파일 찾기
            val midiExtensions = listOf("mid", "midi")
            val baseName = file.nameWithoutExtension

            parentDir.listFiles()?.any { f ->
                val extension = f.extension.lowercase()
                val fileName = f.nameWithoutExtension

                // 같은 이름의 MIDI 파일
                fileName == baseName && midiExtensions.contains(extension)
            } ?: false
        } catch (e: Exception) {
            Log.w(TAG, "Error checking MIDI file: ${e.message}")
            false
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
        if (sheetMusic != null && hasMidiFile(sheetMusic.filePath)) {
            Log.d(TAG, "🎵 MIDI play clicked for: ${sheetMusic.title}")
            analyticsHelper.logEvent("sheet_music_midi_play_clicked",
                mapOf(
                    "sheet_music_id" to sheetMusic.id,
                    "title" to sheetMusic.title
                ))

            // TODO: MIDI 재생 로직 구현
            // 예: MidiPlayer.play(getMidiFilePath(sheetMusic.filePath))
        } else {
            Log.w(TAG, "🚫 No MIDI file available for playback")
        }
    }

    /**
     * 악보 보기 버튼 클릭 이벤트
     */
    fun onScoreViewClicked() {
        val sheetMusic = _uiState.value.sheetMusic
        if (sheetMusic != null && hasScoreFile(sheetMusic.filePath)) {
            Log.d(TAG, "🎼 Score view clicked for: ${sheetMusic.title}")
            analyticsHelper.logEvent("sheet_music_score_view_clicked",
                mapOf(
                    "sheet_music_id" to sheetMusic.id,
                    "title" to sheetMusic.title
                ))

            // TODO: 악보 뷰어 화면으로 이동
            // 예: navigateToScoreViewer(getScoreFilePath(sheetMusic.filePath))
        } else {
            Log.w(TAG, "🚫 No score file available for viewing")
        }
    }

    /**
     * 악보 파일 경로 가져오기 (실제 구현 시 사용)
     */
    private fun getScoreFilePath(basePath: String): String? {
        val file = File(basePath)
        val parentDir = file.parentFile ?: return null
        val baseName = file.nameWithoutExtension
        val scoreExtensions = listOf("pdf", "png", "jpg", "jpeg", "svg")

        return parentDir.listFiles()?.firstOrNull { f ->
            val extension = f.extension.lowercase()
            val fileName = f.nameWithoutExtension

            (fileName == baseName || fileName.contains("score", ignoreCase = true)) &&
                    scoreExtensions.contains(extension)
        }?.absolutePath
    }

    /**
     * MIDI 파일 경로 가져오기 (실제 구현 시 사용)
     */
    private fun getMidiFilePath(basePath: String): String? {
        val file = File(basePath)
        val parentDir = file.parentFile ?: return null
        val baseName = file.nameWithoutExtension
        val midiExtensions = listOf("mid", "midi")

        return parentDir.listFiles()?.firstOrNull { f ->
            val extension = f.extension.lowercase()
            val fileName = f.nameWithoutExtension

            fileName == baseName && midiExtensions.contains(extension)
        }?.absolutePath
    }
}

// UiState도 업데이트 필요할 수 있음
// feature_sheetmusic/ui/util/SheetMusicDetailUiState.kt
/*
data class SheetMusicDetailUiState(
    val isLoading: Boolean = false,
    val sheetMusic: SheetMusicDetail? = null,
    val error: String? = null
) {
    // UI에서 사용할 편의 속성들
    val hasScore: Boolean
        get() = sheetMusic?.let { hasScoreFile(it.filePath) } ?: false

    val hasMidi: Boolean
        get() = sheetMusic?.let { hasMidiFile(it.filePath) } ?: false
}
*/