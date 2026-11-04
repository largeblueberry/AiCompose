package com.largeblueberry.data.repository

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import com.largeblueberry.data.audio.AudioRecordDao
import com.largeblueberry.data.mapper.RecordMapper
import com.largeblueberry.domain.model.RecordModel
import com.largeblueberry.domain.repository.RecordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioRecordDao: AudioRecordDao
) : RecordRepository {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null

    override fun startRecording(): Result<File> {
        return try {
            val cacheDir = context.externalCacheDir
                ?: throw IllegalStateException("External cache directory not available.")
            val outputFile = createUniqueAudioFilePath(cacheDir)
            currentOutputFile = outputFile

            mediaRecorder = (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            Result.success(outputFile)
        } catch (e: IOException) {
            release()
            currentOutputFile = null
            Result.failure(e)
        } catch (e: IllegalStateException) {
            release()
            currentOutputFile = null
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

    override suspend fun saveRecord(recordModel: RecordModel) {
        val audioRecordEntity = RecordMapper.toEntity(recordModel)
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
