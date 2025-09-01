package com.largeblueberry.record.dataLayer.repository.impl

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import com.largeblueberry.local.audio.AudioRecordDao
import com.largeblueberry.record.dataLayer.mapper.RecordMapper
import com.largeblueberry.record.domain.model.RecordModel
import com.largeblueberry.record.domain.repository.RecordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Android 프레임워크의 MediaRecorder를 사용하여 오디오 녹음 기능을 구현
 * AudioRecorder 인터페이스를 구현
 */
class RecordRepositoryImpl @Inject constructor( // 클래스명 변경
    @ApplicationContext private val context: Context,
    private val audioRecordDao: AudioRecordDao
) : RecordRepository {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null



    override fun startRecording(): Result<File> { // outputFile 인자 제거

        return try {
            val cacheDir = context.externalCacheDir
                ?: throw IllegalStateException("External cache directory not available.")
            val outputFile = createUniqueAudioFilePath(cacheDir) // 여기서 파일 경로 생성
            currentOutputFile = outputFile


            mediaRecorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            Result.success(outputFile)
        } catch (e: IOException) {
            // 초기화 실패 시 mediaRecorder를 null로 설정하고 리소스 해제는 하지 않음
            release()
            currentOutputFile = null
            Result.failure(e)
        } catch (e: IllegalStateException) {
            release()
            currentOutputFile = null // 파일도 초기화
            Result.failure(e)
        }
    }

    override fun stopRecording(): Result<Long> {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            val durationMillis = currentOutputFile?.let { file ->
                measureAudioDuration(file.absolutePath)
            } ?: 0L

            Result.success(durationMillis)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalStateException) {
            Result.failure(e)
        } finally {
            release()
        }
    }

    override fun release() {
        mediaRecorder?.release()
        mediaRecorder = null
        currentOutputFile = null
    }

    // AudioRecorder 인터페이스의 insertRecord 메서드를 구현합니다.
    override suspend fun saveRecord(recordModel: RecordModel) {
        // RecordMapper를 사용하여 AudioRecord (도메인 모델)를 AudioRecordEntity (데이터 엔티티)로 변환
        val audioRecordEntity = RecordMapper.toEntity(recordModel)
        // 변환된 엔티티를 DAO를 통해 데이터베이스에 삽입
        audioRecordDao.insertRecord(audioRecordEntity)
    }


    private fun createUniqueAudioFilePath(cacheDir: File): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val now = Date()
        val fileName = "${dateFormat.format(now)}.3gp"
        return File(cacheDir, fileName)
    }

    private fun measureAudioDuration(filePath: String): Long {
        val mediaPlayer = MediaPlayer()
        return try {
            mediaPlayer.setDataSource(filePath)
            mediaPlayer.prepare()
            mediaPlayer.duration.toLong()
        } catch (e: Exception) {
            0L
        } finally {
            mediaPlayer.release()
        }
    }
}
