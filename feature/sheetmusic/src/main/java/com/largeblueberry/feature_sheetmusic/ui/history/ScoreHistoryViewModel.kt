package com.largeblueberry.feature_sheetmusic.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.feature_sheetmusic.domain.repository.SheetMusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScoreHistoryViewModel @Inject constructor(
    private val sheetMusicRepository: SheetMusicRepository
) : ViewModel() {

    val scores: StateFlow<List<SheetMusic>> = sheetMusicRepository.getAllScores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ✅ 악보 삭제 함수 추가
    fun deleteScore(score: SheetMusic) {
        // viewModelScope를 사용하여 IO 작업을 비동기적으로 처리
        viewModelScope.launch {
            sheetMusicRepository.deleteScore(score)
        }
    }
}
