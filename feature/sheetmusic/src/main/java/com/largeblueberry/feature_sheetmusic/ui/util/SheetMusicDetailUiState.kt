package com.largeblueberry.feature_sheetmusic.ui.util

import com.largeblueberry.feature_sheetmusic.domain.SheetMusicDetail

data class SheetMusicDetailUiState(
    val isLoading: Boolean = false,
    val sheetMusic: SheetMusicDetail? = null,
    val error: String? = null
) {
    val hasScore: Boolean get() = sheetMusic?.hasScore == true
    val hasMidi: Boolean get() = sheetMusic?.hasMidi == true
    val canShowScore: Boolean get() = hasScore
    val canPlayMidi: Boolean get() = hasMidi
}