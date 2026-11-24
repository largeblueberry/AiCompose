package com.largeblueberry.library.ui.viemodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.domain.model.LibraryModel
import com.largeblueberry.domain.usecase.library.DeleteAudioRecordUseCase
import com.largeblueberry.domain.usecase.library.GetAudioRecordsUseCase
import com.largeblueberry.domain.usecase.library.RenameAudioRecordUseCase
import com.largeblueberry.domain.usecase.sheetmusic.EnqueueTranscriptionUseCase
import com.largeblueberry.domain.usecase.sheetmusic.GetScoresUseCase
import com.largeblueberry.domain.usecase.sheetmusic.ObserveTranscriptionsUseCase
import com.largeblueberry.library.ui.LibraryUiState
import com.largeblueberry.library.ui.model.RecordTranscription
import com.largeblueberry.library.ui.model.latestScoreByRecord
import com.largeblueberry.library.ui.model.reduceTranscriptions
import com.largeblueberry.library.util.AudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 라이브러리. 채보는 백그라운드 작업에 맡기고(화면을 떠나도 계속), 화면은 작업 상태와 악보 목록을 합쳐 보여 준다.
 * 채보 상태를 ViewModel 메모리에 따로 들지 않는다: 다시 들어와도 같은 두 흐름에서 같은 상태가 나온다.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAudioRecordsUseCase: GetAudioRecordsUseCase,
    private val deleteAudioRecordUseCase: DeleteAudioRecordUseCase,
    private val renameAudioRecordUseCase: RenameAudioRecordUseCase,
    private val enqueueTranscriptionUseCase: EnqueueTranscriptionUseCase,
    private val observeTranscriptionsUseCase: ObserveTranscriptionsUseCase,
    private val getScoresUseCase: GetScoresUseCase,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private companion object {
        private const val TAG = "LibraryViewModel"
    }

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        observeAudioRecords()
        observeTranscriptions()
        audioPlayer.setOnCompletionListener {
            _uiState.update { it.copy(currentPlayingRecordId = null, isPlaying = false) }
        }
    }

    fun deleteRecord(record: LibraryModel) {
        viewModelScope.launch {
            if (uiState.value.currentPlayingRecordId == record.id) stopPlaying()
            val result = deleteAudioRecordUseCase(record)
            result.onFailure { Log.e(TAG, "Failed to delete record: ${record.id}", it) }
            _uiState.update { it.copy(deleteResult = result) }
        }
    }

    fun renameRecord(record: LibraryModel, newName: String) {
        viewModelScope.launch {
            renameAudioRecordUseCase(record, newName)
                .onFailure { Log.e(TAG, "Failed to rename record: ${record.id}", it) }
        }
    }

    /** 녹음을 악보로 만들도록 맡긴다. 이미 대기·진행 중이면 아무 일도 없다(작업이 하나로 유지된다). */
    fun transcribe(record: LibraryModel) = enqueueTranscriptionUseCase(record.id)

    private fun observeAudioRecords() {
        viewModelScope.launch {
            getAudioRecordsUseCase().collect { records ->
                _uiState.update { it.copy(audioRecords = records, isEmpty = records.isEmpty()) }
            }
        }
    }

    /** 두 흐름을 합치고, 직전 상태를 기억하며(scan) 항목 상태를 낸다. 규칙은 [reduceTranscriptions]. */
    private fun observeTranscriptions() {
        viewModelScope.launch {
            combine(observeTranscriptionsUseCase(), getScoresUseCase()) { jobs, scores ->
                jobs to latestScoreByRecord(scores)
            }
                .scan(emptyMap<Int, RecordTranscription>()) { previous, (jobs, scores) ->
                    reduceTranscriptions(previous, jobs, scores)
                }
                .collect { transcriptions -> _uiState.update { it.copy(transcriptions = transcriptions) } }
        }
    }

    fun playAudio(record: LibraryModel) {
        if (audioPlayer.isPlaying()) audioPlayer.stop()
        audioPlayer.play(record.filePath)
        _uiState.update { it.copy(currentPlayingRecordId = record.id, isPlaying = true) }
    }

    fun pauseAudio() {
        audioPlayer.pause()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun resumeAudio() {
        audioPlayer.resume()
        _uiState.update { it.copy(isPlaying = true) }
    }

    fun stopPlaying() {
        audioPlayer.stop()
        _uiState.update { it.copy(currentPlayingRecordId = null, isPlaying = false) }
    }

    fun clearDeleteResult() = _uiState.update { it.copy(deleteResult = null) }

    fun showRenameDialog(record: LibraryModel) = _uiState.update { it.copy(showRenameDialogForRecord = record) }

    fun dismissRenameDialog() = _uiState.update { it.copy(showRenameDialogForRecord = null) }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
