package com.largeblueberry.feature_sheetmusic.ui.state

import com.largeblueberry.domain.model.SheetMusic

/** 악보 상세 화면 상태. 화면은 이 값만 보고 그린다(단방향). */
sealed interface SheetMusicUiState {
    data object Loading : SheetMusicUiState
    data class Success(val sheetMusic: SheetMusic, val musicXml: String) : SheetMusicUiState
    /** 지워졌거나 잘못된 id */
    data object NotFound : SheetMusicUiState
    /** 파일을 읽지 못함 등. 다시 시도할 수 있다. */
    data object Error : SheetMusicUiState
}
