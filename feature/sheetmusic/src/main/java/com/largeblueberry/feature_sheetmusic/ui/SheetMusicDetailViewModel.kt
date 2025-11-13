package com.largeblueberry.feature_sheetmusic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SheetMusicDetailUiState(
    val isLoading: Boolean = false,
    val sheetMusic: SheetMusic? = null,
    val error: String? = null
)

class SheetMusicDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SheetMusicDetailUiState())
    val uiState: StateFlow<SheetMusicDetailUiState> = _uiState.asStateFlow()

    // Mock 데이터 (나중에 서버 연결시 Repository로 교체)
    private val mockSheetMusicList = listOf(
        SheetMusic("1", "Spring Day", "BTS", "3:47", "2024.11.06"),
        SheetMusic("2", "Dynamite", "BTS", "3:19", "2024.11.05"),
        SheetMusic("3", "Butter", "BTS", "2:44", "2024.11.04"),
        SheetMusic("4", "Permission to Dance", "BTS", "3:07", "2024.11.03"),
        SheetMusic("5", "Life Goes On", "BTS", "3:27", "2024.11.02")
    )

    fun loadSheetMusic(sheetMusicId: String?) {
        if (sheetMusicId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "잘못된 악보 ID입니다."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Mock 서버 호출 시뮬레이션 (지연시간)
                kotlinx.coroutines.delay(500)

                val sheetMusic = mockSheetMusicList.find { it.id == sheetMusicId }

                if (sheetMusic != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        sheetMusic = sheetMusic
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "해당 악보를 찾을 수 없습니다."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "악보 로딩 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }

    fun retry(sheetMusicId: String?) {
        loadSheetMusic(sheetMusicId)
    }
}