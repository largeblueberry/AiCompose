package com.largeblueberry.library.ui.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.largeblueberry.remote.model.UploadState
import com.largeblueberry.remote.model.UploadStatus
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
    private val userUsageRepository: UserUsageRepository
) : ViewModel() {


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
                _uiState.update { it.copy(deleteResult = Result.success(Unit)) }
                // 만약 삭제된 레코드가 현재 재생 중이었다면 정지
                if (uiState.value.currentPlayingRecordId == record.id) {
                    stopPlaying()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(deleteResult = Result.failure(e)) }
            }
        }
    }

    // 이름 변경 가능 함수
    fun renameRecord(record: LibraryModel, newName: String) {
        viewModelScope.launch {
            try {
                renameAudioRecordUseCase(record, newName) // invoke() 호출
                // 필요하다면 이름 변경 성공/실패에 대한 StateFlow를 추가
            } catch (e: Exception) {
                // 필요하다면 오류 메시지 전달용 StateFlow 추가
            }
        }
    }

    //서버 전송 함수, 비즈니스로직은 UploadAudioRecordUseCase에 위임
    //여기서는 오직 업로드 상태 관리와 UI 업데이트만 담당
    fun uploadAudioToServer(filePath: String, recordId: Int) {
        viewModelScope.launch {

            if (_uiState.value.isUploadingInProgress) {
                _uiState.update { it.copy(showUploadInProgressMessage = true) }
                return@launch
            }

            //업로드 가능 여부 체크
            val availabilityResult = checkUploadAvailabilityUseCase.invoke()

            when(availabilityResult){
                is UploadAvailabilityResult.Available ->{
                    _uiState.update {
                        it.copy(
                            uploadState = UploadState(
                                status = UploadStatus.UPLOADING,
                                recordId = recordId
                            ),
                            uploadingRecordId = recordId,
                            isUploadingInProgress = true // 업로드 시작 시 플래그 설정
                        )
                    }
                    try {
                        val result: Result<String> = uploadAudioRecordUseCase(filePath)

                        result.fold(
                            onSuccess = { midiUrl ->
                                if (midiUrl.isNotEmpty()) {
                                    _uiState.update {
                                        it.copy(
                                            uploadState = UploadState(
                                                status = UploadStatus.SUCCESS,
                                                url = midiUrl,
                                                recordId = recordId,
                                            ),
                                            currentUploads = availabilityResult.currentUploads,
                                            maxUploads = availabilityResult.maxUploads
                                        )
                                    }
                                    checkUploadAvailabilityUseCase.uploadCounter()
                                } else {
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
                        // 업로드 완료 후 uploadingRecordId 초기화.
                        // 여러 업로드를 동시에 처리하지 않는다는 가정 하에.
                        if (_uiState.value.uploadingRecordId == recordId) {
                            _uiState.update {
                                it.copy(
                                    uploadingRecordId = null,
                                    isUploadingInProgress = false
                                    // 업로드 결과에 상관없이 시 플래그
                                )
                            }
                        }
                    }
                }
                is UploadAvailabilityResult.LimitReached -> {
                    // 업로드 한도 초과: 사용자에게 메시지 표시
                    _uiState.update {
                        it.copy(
                            uploadState = UploadState(
                                status = UploadStatus.ERROR,
                                message = "업로드 한도 초과: 최대 ${availabilityResult.maxUploads}회 업로드 가능합니다.",
                                recordId = recordId // 어떤 레코드에 대한 메시지인지 표시
                            ),
                            currentUploads = availabilityResult.currentUploads,
                            maxUploads = availabilityResult.maxUploads
                        )
                    }
                }
                is UploadAvailabilityResult.Error -> {
                // 업로드 가능 여부 확인 중 오류 발생
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