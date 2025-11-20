package com.largeblueberry.feature_sheetmusic.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.feature_sheetmusic.domain.repository.SheetMusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ScoreHistoryViewModel @Inject constructor(
    sheetMusicRepository: SheetMusicRepository
) : ViewModel() {

    // ✅ Repository로부터 모든 악보 목록을 Flow로 받아 StateFlow로 변환
    val scores: StateFlow<List<SheetMusic>> = sheetMusicRepository.getAllScores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // 5초 동안 구독자가 없으면 Flow 중지
            initialValue = emptyList() // 초기값은 빈 리스트
        )
}
