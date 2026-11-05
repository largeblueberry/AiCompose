package com.largeblueberry.library.ui.viemodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.domain.model.LibraryModel
import com.largeblueberry.domain.usecase.library.DeleteAudioRecordUseCase
import com.largeblueberry.domain.usecase.library.GetAudioRecordsUseCase
import com.largeblueberry.domain.usecase.library.RenameAudioRecordUseCase
import com.largeblueberry.library.ui.LibraryUiState
import com.largeblueberry.library.ui.model.UploadState
import com.largeblueberry.library.ui.model.UploadStatus
import com.largeblueberry.library.util.AudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAudioRecordsUseCase: GetAudioRecordsUseCase,
    private val deleteAudioRecordUseCase: DeleteAudioRecordUseCase,
    private val renameAudioRecordUseCase: RenameAudioRecordUseCase,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private companion object {
        private const val TAG = "LibraryViewModel"
    }

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        observeAudioRecords()
        audioPlayer.setOnCompletionListener {
            _uiState.update { it.copy(currentPlayingRecordId = null, isPlaying = false) }
        }
    }

    fun deleteRecord(record: LibraryModel) {
        viewModelScope.launch {
            try {
                deleteAudioRecordUseCase(record)
                Log.i(TAG, "Record deleted successfully: ${record.id}")

                _uiState.update { it.copy(deleteResult = Result.success(Unit)) }
                if (uiState.value.currentPlayingRecordId == record.id) {
                    stopPlaying()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete record: ${record.id}", e)
                _uiState.update { it.copy(deleteResult = Result.failure(e)) }
            }
        }
    }

    fun renameRecord(record: LibraryModel, newName: String) {
        Log.d(TAG, "Attempting to rename record: ${record.id} to $newName")
        viewModelScope.launch {
            try {
                renameAudioRecordUseCase(record, newName)
                Log.i(TAG, "Record renamed successfully: ${record.id} to $newName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to rename record: ${record.id}", e)
            }
        }
    }

    fun uploadAudioToServer(filePath: String, recordId: Int) {
        viewModelScope.launch {
            if (_uiState.value.isUploadingInProgress) {
                _uiState.update { it.copy(showUploadInProgressMessage = true) }
                return@launch
            }

            _uiState.update {
                it.copy(
                    uploadState = UploadState(
                        status = UploadStatus.UPLOADING,
                        recordId = recordId
                    ),
                    uploadingRecordId = recordId,
                    isUploadingInProgress = true
                )
            }
            try {
                // 온디바이스 AI 채보 변환 준비 중 (스텁)
                Log.i(TAG, "On-device AI transcription processing for record: $recordId, file: $filePath")
                _uiState.update {
                    it.copy(
                        uploadState = UploadState(
                            status = UploadStatus.SUCCESS,
                            scoreUrl = "",
                            midiUrl = "",
                            recordId = recordId
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "An unexpected error occurred during processing for record: $recordId", e)
                _uiState.update {
                    it.copy(
                        uploadState = UploadState(
                            status = UploadStatus.ERROR,
                            message = e.message ?: "알 수 없는 에러 발생",
                            recordId = recordId
                        )
                    )
                }
            } finally {
                if (_uiState.value.uploadingRecordId == recordId) {
                    _uiState.update {
                        it.copy(
                            uploadingRecordId = null,
                            isUploadingInProgress = false
                        )
                    }
                }
            }
        }
    }

    private fun observeAudioRecords() {
        viewModelScope.launch {
            getAudioRecordsUseCase().collect { records ->
                _uiState.update { currentState ->
                    currentState.copy(
                        audioRecords = records,
                        isEmpty = records.isEmpty()
                    )
                }
            }
        }
    }

    fun playAudio(record: LibraryModel) {
        Log.d(TAG, "Playing audio for record: ${record.id}")
        if (audioPlayer.isPlaying()) {
            audioPlayer.stop()
        }
        audioPlayer.play(record.filePath)
        _uiState.update {
            it.copy(
                currentPlayingRecordId = record.id,
                isPlaying = true
            )
        }
    }

    fun pauseAudio() {
        Log.d(TAG, "Pausing audio")
        audioPlayer.pause()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun resumeAudio() {
        Log.d(TAG, "Resuming audio")
        audioPlayer.resume()
        _uiState.update { it.copy(isPlaying = true) }
    }

    fun stopPlaying() {
        Log.d(TAG, "Stopping audio")
        audioPlayer.stop()
        _uiState.update { it.copy(currentPlayingRecordId = null, isPlaying = false) }
    }

    fun clearDeleteResult() {
        _uiState.update { it.copy(deleteResult = null) }
    }

    fun clearUploadState() {
        _uiState.update { it.copy(uploadState = UploadState()) }
    }

    fun showRenameDialog(record: LibraryModel) {
        _uiState.update { it.copy(showRenameDialogForRecord = record) }
    }

    fun dismissRenameDialog() {
        _uiState.update { it.copy(showRenameDialogForRecord = null) }
    }

    fun clearUploadInProgressMessage() {
        _uiState.update { it.copy(showUploadInProgressMessage = false) }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}