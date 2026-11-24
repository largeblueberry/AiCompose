package com.largeblueberry.feature_sheetmusic.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.domain.usecase.sheetmusic.GetSheetMusicDetailUseCase
import com.largeblueberry.feature_sheetmusic.ui.state.SheetMusicUiState
import com.largeblueberry.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 악보 상세. 악보 id는 내비게이션 인자로 받아(SavedStateHandle) 스스로 불러온다.
 * 화면이 "언제 불러올지"를 정하지 않는다(docs/troubleshooting.md C-5).
 */
@HiltViewModel
class SheetMusicViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSheetMusicDetailUseCase: GetSheetMusicDetailUseCase,
) : ViewModel() {

    private val scoreId: String? = savedStateHandle[AppRoutes.SheetMusicScreen.SCORE_ID_ARG]

    private val _uiState = MutableStateFlow<SheetMusicUiState>(SheetMusicUiState.Loading)
    val uiState: StateFlow<SheetMusicUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        val id = scoreId ?: run {
            _uiState.value = SheetMusicUiState.NotFound
            return
        }
        _uiState.value = SheetMusicUiState.Loading
        viewModelScope.launch {
            getSheetMusicDetailUseCase(id)
                .onSuccess { detail ->
                    _uiState.value = if (detail == null) SheetMusicUiState.NotFound
                    else SheetMusicUiState.Success(detail.sheetMusic, detail.musicXml)
                }
                .onFailure { e ->
                    Log.e(TAG, "load failed: $id", e)
                    _uiState.value = SheetMusicUiState.Error
                }
        }
    }

    private companion object {
        const val TAG = "SheetMusicViewModel"
    }
}
