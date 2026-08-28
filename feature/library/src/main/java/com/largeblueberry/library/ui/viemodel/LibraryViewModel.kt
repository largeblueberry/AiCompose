package com.largeblueberry.library.ui.viemodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.analyticshelper.AnalyticsHelper
import com.largeblueberry.domain.AuthGateway
import com.largeblueberry.domain.model.UploadAvailabilityResult
import com.largeblueberry.domain.repository.UserUsageRepository
import com.largeblueberry.domain.usecase.CheckUploadAvailabilityUseCase
import com.largeblueberry.library.domainLayer.model.LibraryModel
import com.largeblueberry.library.domainLayer.usecase.DeleteAudioRecordUseCase
import com.largeblueberry.library.domainLayer.usecase.GetAudioRecordsUseCase
import com.largeblueberry.library.domainLayer.usecase.RenameAudioRecordUseCase
import com.largeblueberry.library.domainLayer.usecase.UploadAudioRecordUseCase
import com.largeblueberry.library.ui.LibraryUiState
import com.largeblueberry.library.util.AudioPlayer
import com.largeblueberry.network.model.request.UploadState
import com.largeblueberry.network.model.request.UploadStatus
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
    private val uploadAudioRecordUseCase: UploadAudioRecordUseCase,
    private val checkUploadAvailabilityUseCase: CheckUploadAvailabilityUseCase,
    private val audioPlayer: AudioPlayer,
    private val authGateway: AuthGateway,
    private val userUsageRepository: UserUsageRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private companion object {
        private const val TAG = "LibraryViewModel"
    }

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
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
                analyticsHelper.logEvent("delete_record_success", mapOf("record_id" to record.id.toString()))

                _uiState.update { it.copy(deleteResult = Result.success(Unit)) }
                if (uiState.value.currentPlayingRecordId == record.id) {
                    stopPlaying()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete record: ${record.id}", e)
                analyticsHelper.logEvent(
                    "delete_record_failure",
                    mapOf("record_id" to record.id.toString(), "error" to (e.message ?: "Unknown error"))
                )

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
                analyticsHelper.logEvent("rename_record_success", mapOf("record_id" to record.id.toString()))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to rename record: ${record.id}", e)
                analyticsHelper.logEvent(
                    "rename_record_failure",
                    mapOf("record_id" to record.id.toString(), "error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }

    fun uploadAudioToServer(filePath: String, recordId: Int) {
        viewModelScope.launch {

            if (_uiState.value.isUploadingInProgress) {
                _uiState.update { it.copy(showUploadInProgressMessage = true) }
                return@launch
            }

            when(val availabilityResult = checkUploadAvailabilityUseCase.invoke()){
                is UploadAvailabilityResult.Available ->{
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
                        val result = uploadAudioRecordUseCase(filePath)

                        result.fold(
                            onSuccess = { uploadResponse ->
                                val scoreUrl = uploadResponse.scoreUrl
                                val midiUrl = uploadResponse.midiUrl

                                if (!scoreUrl.isNullOrEmpty() && !midiUrl.isNullOrEmpty()) {
                                    Log.i(TAG, "Upload successful for record: $recordId. Score URL: $scoreUrl, MIDI URL: $midiUrl")
                                    analyticsHelper.logEvent("upload_record_success", mapOf("record_id" to recordId.toString()))

                                    _uiState.update {
                                        it.copy(
                                            uploadState = UploadState(
                                                status = UploadStatus.SUCCESS,
                                                scoreUrl = scoreUrl,
                                                midiUrl = midiUrl,
                                                recordId = recordId
                                            ),
                                            currentUploads = availabilityResult.currentUploads,
                                            maxUploads = availabilityResult.maxUploads
                                        )
                                    }
                                    checkUploadAvailabilityUseCase.uploadCounter()
                                } else {
                                    Log.w(TAG, "Upload for record $recordId succeeded but returned an empty or invalid URL.")
                                    analyticsHelper.logEvent("upload_record_empty_url", mapOf("record_id" to recordId.toString()))
                                    _uiState.update {
                                        it.copy(
                                            uploadState = UploadState(
                                                status = UploadStatus.ERROR,
                                                message = "업로드 성공했으나, 유효한 URL을 받지 못했습니다.",
                                                recordId = recordId
                                            )
                                        )
                                    }
                                }
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "Upload failed for record: $recordId", exception)
                                analyticsHelper.logEvent(
                                    "upload_record_failure",
                                    mapOf("record_id" to recordId.toString(), "error" to (exception.message ?: "Unknown error"))
                                )
                                _uiState.update {
                                    it.copy(
                                        uploadState = UploadState(
                                            status = UploadStatus.ERROR,
                                            message = exception.message ?: "알 수 없는 에러 발생",
                                            recordId = recordId
                                        )
                                    )
                                }
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "An unexpected error occurred during upload for record: $recordId", e)
                        analyticsHelper.logEvent(
                            "upload_record_exception",
                            mapOf("record_id" to recordId.toString(), "error" to (e.message ?: "Unknown error"))
                        )
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

                is UploadAvailabilityResult.LimitReached -> {
                    Log.w(TAG, "Upload limit reached for user. Max: ${availabilityResult.maxUploads}")
                    analyticsHelper.logEvent("upload_limit_reached", mapOf("max_uploads" to availabilityResult.maxUploads.toString()))
                    _uiState.update {
                        it.copy(
                            uploadState = UploadState(
                                status = UploadStatus.ERROR,
                                message = "업로드 한도 초과: 최대 ${availabilityResult.maxUploads}회 업로드 가능합니다.",
                                recordId = recordId
                            ),
                            currentUploads = availabilityResult.currentUploads,
                            maxUploads = availabilityResult.maxUploads
                        )
                    }
                }
                is UploadAvailabilityResult.Error -> {
                    Log.e(TAG, "Error checking upload availability: ${availabilityResult.message}")
                    analyticsHelper.logEvent("upload_availability_check_failure", mapOf("error" to availabilityResult.message))
                    _uiState.update {
                        it.copy(
                            uploadState = UploadState(
                                status = UploadStatus.ERROR,
                                message = availabilityResult.message,
                                recordId = recordId
                            )
                        )
                    }
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val userId = authGateway.getCurrentUserId()
            val maxUploads = authGateway.getUploadLimitForUser(userId)
            val currentUploads = userUsageRepository.getCurrentUploadCount(userId)

            _uiState.update {
                it.copy(
                    maxUploads = maxUploads,
                    currentUploads = currentUploads
                )
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

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun playAudio(record: LibraryModel) {
        Log.d(TAG, "Playing audio for record: ${record.id}")
        analyticsHelper.logEvent("play_audio", mapOf("record_id" to record.id.toString()))
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
        analyticsHelper.logEvent("pause_audio", emptyMap())
        audioPlayer.pause()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun resumeAudio() {
        Log.d(TAG, "Resuming audio")
        analyticsHelper.logEvent("resume_audio", emptyMap())
        audioPlayer.resume()
        _uiState.update { it.copy(isPlaying = true) }
    }

    fun stopPlaying() {
        Log.d(TAG, "Stopping audio")
        analyticsHelper.logEvent("stop_audio", emptyMap())
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