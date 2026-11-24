package com.largeblueberry.feature_sheetmusic.ui.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.usecase.sheetmusic.DeleteScoreUseCase
import com.largeblueberry.domain.usecase.sheetmusic.GetScoresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 악보 목록. 저장소가 아니라 유스케이스를 받는다(docs/troubleshooting.md C-4). */
@HiltViewModel
class ScoreHistoryViewModel @Inject constructor(
    getScoresUseCase: GetScoresUseCase,
    private val deleteScoreUseCase: DeleteScoreUseCase,
) : ViewModel() {

    val scores: StateFlow<List<SheetMusic>> = getScoresUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteScore(score: SheetMusic) {
        viewModelScope.launch {
            deleteScoreUseCase(score).onFailure { Log.e("ScoreHistoryViewModel", "delete failed: ${score.id}", it) }
        }
    }
}
