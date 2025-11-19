package com.largeblueberry.library.ui.viemodel

import android.util.Log
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

    // Logcat 필터링을 위한 TAG 추가
    private companion object {
        private const val TAG = "LibraryViewModel"
    }

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        // 오디오 레코드 리스트와 isEmpty 상태를 함께 업데이트
        observeAudioRecords()

        // AudioPlayer 재생 완료 리스너 설정
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
                // 만약 삭제된 레코드가 현재 재생 중이었다면 정지
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

    // 이름 변경 가능 함수
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

    //서버 전송 함수, 비즈니스로직은 UploadAudioRecordUseCase에 위임
    //여기서는 오직 업로드 상태 관리와 UI 업데이트만 담당
    // in LibraryViewModel.kt

    fun uploadAudioToServer(filePath: String, recordId: Int) {
        viewModelScope.launch {

            if (_uiState.value.isUploadingInProgress) {
                _uiState.update { it.copy(showUploadInProgressMessage = true) }
                return@launch
            }

            val availabilityResult = checkUploadAvailabilityUseCase.invoke()

            when(availabilityResult){
                is UploadAvailabilityResult.Available ->{
                    _uiState.update {
                        it.copy(
                            // 🔥 수정: UploadState 생성 시 url 필드는 이제 없습니다.
                            uploadState = UploadState(
                                status = UploadStatus.UPLOADING,
                                recordId = recordId
                            ),
                            uploadingRecordId = recordId,
                            isUploadingInProgress = true
                        )
                    }
                    try {
                        // uploadAudioRecordUseCase는 Result<UploadResponse>를 반환한다고 가정합니다.
                        // UploadResponse는 scoreUrl과 midiUrl을 모두 포함해야 합니다.
                        val result = uploadAudioRecordUseCase(filePath)

                        result.fold(
                            onSuccess = { uploadResponse ->
                                // ✅ scoreUrl과 midiUrl을 모두 가져옵니다.
                                val scoreUrl = uploadResponse.scoreUrl
                                val midiUrl = uploadResponse.midiUrl

                                // ✅ 두 URL이 모두 유효한지 확인합니다.
                                if (!scoreUrl.isNullOrEmpty() && !midiUrl.isNullOrEmpty()) {
                                    Log.i(TAG, "Upload successful for record: $recordId. Score URL: $scoreUrl, MIDI URL: $midiUrl")
                                    analyticsHelper.logEvent("upload_record_success", mapOf("record_id" to recordId.toString()))

                                    _uiState.update {
                                        it.copy(
                                            // 🔥 수정: 새로운 UploadState 구조에 맞게 두 URL을 모두 저장합니다.
                                            uploadState = UploadState(
                                                status = UploadStatus.SUCCESS,
                                                scoreUrl = scoreUrl, // ✅ scoreUrl 저장
                                                midiUrl = midiUrl,   // ✅ midiUrl 저장
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
                // ... (LimitReached, Error 케이스는 동일)
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


    // 오디오 재생 관련 함수들
    fun playAudio(record: LibraryModel) {
        Log.d(TAG, "Playing audio for record: ${record.id}")
        analyticsHelper.logEvent("play_audio", mapOf("record_id" to record.id.toString()))
        // 이미 재생 중인 레코드가 있다면 정지
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

    // UI 상태 초기화 함수들
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
        audioPlayer.stop() // ViewModel이 파괴될 때 MediaPlayer도 해제
    }
}