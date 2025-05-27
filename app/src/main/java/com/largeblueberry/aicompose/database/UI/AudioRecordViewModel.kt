package com.largeblueberry.aicompose.database.UI

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.largeblueberry.aicompose.record.database.AudioDatabase
import com.largeblueberry.aicompose.record.database.AudioRecordEntity
import com.largeblueberry.aicompose.retrofit.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class AudioRecordViewModel(context: Context) : ViewModel() {

    private val audioRecordDao = AudioDatabase.getDatabase(context).audioRecordDao()

    // 녹음 기록 리스트
    val audioRecords: StateFlow<List<AudioRecordEntity>> = audioRecordDao.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),// 마지막 구독 5초 후에 구독 해제
            initialValue = emptyList() // 초기값은 빈 리스트
        )// stateflow 를 활용한 개발

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
                val fileDeleted = File(record.filePath).delete()
                audioRecordDao.deleteRecord(record)
                if (!fileDeleted) {
                    // 파일이 이미 없거나 삭제 실패
                    _deleteResult.value = Result.failure(Exception("파일 삭제 실패"))
                } else {
                    _deleteResult.value = Result.success(Unit)
                }
            } catch (e: Exception) {
                _deleteResult.value = Result.failure(e)
            }
        }
    }

    //이름 변경 가능 함수
    fun renameRecord(record: AudioRecordEntity, newName: String) {
        viewModelScope.launch {
            try {
                val oldFile = File(record.filePath)
                val parentDir = oldFile.parentFile
                val fileExtension = oldFile.extension
                // 확장자 유지
                val newFileName = if (fileExtension.isNotEmpty()) "$newName.$fileExtension" else newName
                val newFile = File(parentDir, newFileName)

                // 실제 파일 이름 변경
                val renamed = oldFile.renameTo(newFile)
                if (renamed) {
                    // DB의 파일명, 경로 업데이트
                    val updatedRecord = record.copy(
                        filename = newFileName,
                        filePath = newFile.absolutePath
                    )
                    audioRecordDao.updateRecord(updatedRecord)
                } else {
                    // 파일 이름 변경 실패
                    
                    // 필요하다면 오류 메시지 전달용 StateFlow 추가 가능
                }
            } catch (e: Exception) {
                // 필요하다면 오류 메시지 전달용 StateFlow 추가 가능
            }
        }
    }

    fun uploadAudioToServer(filePath: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    onResult(false, "파일이 존재하지 않습니다.")
                    return@launch
                }

                // MultipartBody.Part 생성
                val requestFile = RequestBody.create("audio/*".toMediaTypeOrNull(), file)
                val audioPart = MultipartBody.Part.createFormData(
                    "audio",
                    file.name,
                    requestFile
                )

                // description 생성
                val description = RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    "audio file"
                )

                // API 호출
                val response = RetrofitClient.audioUploadService.upload3gpFile(audioPart, description)

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        if (result.success && result.url != null) {
                            onResult(true, result.url)
                        } else {
                            onResult(false, result.message)
                        }
                    }
                } else {
                    onResult(false, "서버 오류: ${response.code()}")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "업로드 중 오류 발생")
            }
        }
    }

    // 삭제 결과 상태 초기화 (UI에서 메시지 표시 후 호출)
    fun clearDeleteResult() {
        _deleteResult.value = null
    }
}