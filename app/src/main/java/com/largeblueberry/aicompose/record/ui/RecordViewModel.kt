package com.largeblueberry.aicompose.record.ui

import android.app.Application
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.largeblueberry.aicompose.record.database.AudioDatabase
import com.largeblueberry.aicompose.record.database.AudioRecordEntity
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordViewModel(application: Application) : AndroidViewModel(application) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""
    private val context = getApplication<Application>().applicationContext

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _recordingStateText = MutableLiveData("대기 중")
    val recordingStateText: LiveData<String> = _recordingStateText

    private val _lastSavedFileName = MutableLiveData<String>()
    val lastSavedFileName: LiveData<String> = _lastSavedFileName

    private val db = AudioDatabase.getDatabase(context)
    private val dao = db.audioRecordDao()

    fun startRecording() {
        try {
            outputFile = createUniqueAudioFilePath()
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                prepare()
                start()
            }
            _isRecording.value = true
            _recordingStateText.value = "녹음 중..."
        } catch (e: Exception) {
            _recordingStateText.value = "녹음 시작 실패: ${e.message}"
            mediaRecorder?.release()
            mediaRecorder = null
            _isRecording.value = false
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false
            _recordingStateText.value = "녹음 완료"

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(outputFile)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration
            mediaPlayer.release()

            saveRecordingToDatabase(outputFile, formatRecordingDuration(duration))
        } catch (e: Exception) {
            _recordingStateText.value = "녹음 중지 실패: ${e.message}"
        }
    }

    private fun createUniqueAudioFilePath(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val now = Date()
        val fileName = "${dateFormat.format(now)}.3gp"
        return "${context.externalCacheDir?.absolutePath}/$fileName"
    }

    private fun formatRecordingDuration(durationMillis: Int): String {
        val minutes = (durationMillis / 1000) / 60
        val seconds = (durationMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun saveRecordingToDatabase(filePath: String, duration: String) {
        val fileName = filePath.substring(filePath.lastIndexOf("/") + 1)
        val createdAt = System.currentTimeMillis()
        val audioRecord = AudioRecordEntity(
            filename = fileName,
            filePath = filePath,
            fileSize = File(filePath).length(), // 파일 크기 추가
            createdAt = createdAt,
            duration = duration
        )
        viewModelScope.launch {
            dao.insertRecord(audioRecord)
            _lastSavedFileName.postValue(fileName)
        }
    }

}