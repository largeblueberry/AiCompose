package com.largeblueberry.feature_sheetmusic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.feature_sheetmusic.domain.GenerateSheetMusicUseCase
import com.largeblueberry.feature_sheetmusic.ui.state.SheetMusicUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SheetMusicViewModel @Inject constructor(
    private val generateSheetMusicUseCase: GenerateSheetMusicUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SheetMusicUiState>(SheetMusicUiState.Idle)
    val uiState: StateFlow<SheetMusicUiState> = _uiState.asStateFlow()

    fun generateSheetMusic(requestBody: Any) {
        viewModelScope.launch {
            _uiState.value = SheetMusicUiState.Loading

            generateSheetMusicUseCase(requestBody)
                .onSuccess { sheetMusic ->
                    _uiState.value = SheetMusicUiState.Success(sheetMusic)
                }
                .onFailure { exception ->
                    _uiState.value = SheetMusicUiState.Error(
                        exception.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = SheetMusicUiState.Idle
    }
}