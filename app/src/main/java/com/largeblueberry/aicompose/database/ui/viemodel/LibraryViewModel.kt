package com.largeblueberry.aicompose.database.ui.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.aicompose.dataLayer.model.local.AudioRecordEntity
import com.largeblueberry.aicompose.dataLayer.model.network.UploadState
import com.largeblueberry.aicompose.dataLayer.model.network.UploadStatus
import com.largeblueberry.aicompose.library.domainLayer.usecase.DeleteAudioRecordUseCase
import com.largeblueberry.aicompose.library.domainLayer.usecase.GetAudioRecordsUseCase
import com.largeblueberry.aicompose.library.domainLayer.usecase.RenameAudioRecordUseCase
import com.largeblueberry.aicompose.library.domainLayer.usecase.UploadAudioRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAudioRecordsUseCase: GetAudioRecordsUseCase,
    private val deleteAudioRecordUseCase: DeleteAudioRecordUseCase,
    private val renameAudioRecordUseCase: RenameAudioRecordUseCase,
    private val uploadAudioRecordUseCase: UploadAudioRecordUseCase
) : ViewModel() {

    // 업로드 상태 추가
    private val _uploadState = MutableStateFlow(UploadState())
    val uploadState: StateFlow<UploadState> = _uploadState
    private val _uploadingRecordId = MutableStateFlow<Int?>(null)
    val uploadingRecordId: StateFlow<Int?> = _uploadingRecordId


    // 녹음 기록 리스트
    // audioRecordDao 대신 getAudioRecordsUseCase를 사용합니다.
    val audioRecords: StateFlow<List<AudioRecordEntity>> = getAudioRecordsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // 마지막 구독 5초 후에 구독 해제
            initialValue = emptyList() // 초기값은 빈 리스트
        )

    // UI 상태: 데이터가 비어있는지 여부
    private val _isEmpty = MutableStateFlow(true)
    val isEmpty: StateFlow<Boolean> = _isEmpty

    // 삭제 성공/실패 상태 알림용
    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    init {
        // 데이터가 비어있는지 여부를 업데이트
        viewModelScope.launch {
            audioRecords.collect { records ->
                _isEmpty.value = records.isEmpty()
            }
        }
    }

    fun deleteRecord(record: AudioRecordEntity) {
        viewModelScope.launch {
            try {
                // 파일 삭제 및 DB 업데이트 로직은 DeleteAudioRecordUseCase가 처리하도록 위임합니다.
                deleteAudioRecordUseCase(record) // invoke() 호출
                _deleteResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _deleteResult.value = Result.failure(e)
            }
        }
    }

    // 이름 변경 가능 함수
    fun renameRecord(record: AudioRecordEntity, newName: String) {
        viewModelScope.launch {
            try {
                renameAudioRecordUseCase(record, newName) // invoke() 호출
                // 필요하다면 이름 변경 성공/실패에 대한 StateFlow를 추가할 수 있습니다.
            } catch (e: Exception) {
                // 필요하다면 오류 메시지 전달용 StateFlow 추가 가능

            }
        }
    }

    fun uploadAudioToServer(filePath: String, recordId: Int) { // recordId는 UI에서 Int로 넘어오므로 non-nullable
        viewModelScope.launch {
            _uploadingRecordId.value = recordId // UI에서 어떤 레코드가 업로드 중인지 식별
            _uploadState.value = UploadState(
                status = UploadStatus.UPLOADING,
                recordId = recordId // UI 상태에 recordId 포함
            )

            try {
                // UploadAudioRecordUseCase는 이제 kotlin.Result<String>을 반환하며, recordId를 받지 않습니다.
                val result: Result<String> = uploadAudioRecordUseCase(filePath) // recordId 제거

                result.fold(
                    onSuccess = { midiUrl ->
                        if (midiUrl.isNotEmpty()) {
                            _uploadState.value = UploadState(status = UploadStatus.SUCCESS, url = midiUrl, recordId = recordId)
                        } else {
                            _uploadState.value = UploadState(status = UploadStatus.ERROR, message = "업로드 성공했으나, 유효한 URL을 받지 못했습니다.", recordId = recordId)
                        }
                    },
                    onFailure = { exception ->
                        _uploadState.value = UploadState(status = UploadStatus.ERROR, message = exception.message ?: "알 수 없는 에러 발생", recordId = recordId)
                    }
                )

            } catch (e: Exception) {
                _uploadState.value = UploadState(status = UploadStatus.ERROR, message = e.message ?: "알 수 없는 에러 발생", recordId = recordId)
            } finally {
                // 현재 처리중인 recordId에 대한 작업이 끝났을 때만 null로 설정
                // (동시에 여러 업로드를 처리하지 않는다는 가정 하에)
                if (_uploadingRecordId.value == recordId) {
                    _uploadingRecordId.value = null
                }
            }
        }
    }


    // 삭제 결과 상태 초기화 (UI에서 메시지 표시 후 호출)
    fun clearDeleteResult() {
        _deleteResult.value = null
    }

    fun clearUploadState() {
        _uploadState.value = UploadState()
    }
}