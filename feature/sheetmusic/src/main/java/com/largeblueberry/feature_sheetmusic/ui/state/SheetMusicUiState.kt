package com.largeblueberry.feature_sheetmusic.ui.state

import com.largeblueberry.feature_sheetmusic.domain.SheetMusic


sealed class SheetMusicUiState {
    object Idle : SheetMusicUiState()
    object Loading : SheetMusicUiState()
    data class Success(val sheetMusic: SheetMusic) : SheetMusicUiState()
    data class Error(val message: String) : SheetMusicUiState()
}