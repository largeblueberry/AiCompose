package com.largeblueberry.aicompose.record

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.largeblueberry.aicompose.database.UI.AudioRecordDataActivity
import com.largeblueberry.aicompose.databinding.ActivityRecordBinding
import com.largeblueberry.aicompose.record.database.AudioDatabase
import kotlinx.coroutines.launch

class RecordActivity : AppCompatActivity() {
    /* 녹음 화면 임 여기서 녹음기 키고 녹음함.*/

    private lateinit var binding: ActivityRecordBinding // 뷰 바인딩 객체
    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""
    private lateinit var database: AudioDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(
            applicationContext,
            AudioDatabase::class.java,
            "audio_database"
        ).build()

        // 뷰 바인딩 초기화
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 기본 상태 설정
        binding.btnStop.visibility = View.GONE
        binding.progressRecording.visibility = View.GONE

        // 녹음 파일 경로 설정
        outputFile = "${externalCacheDir?.absolutePath}/recorded_audio.3gp"

        // 녹음 시작 버튼 클릭 리스너
        binding.btnRecord.setOnClickListener {
            checkPermissionAndStartRecording()
        }

        // 녹음 중지 버튼 클릭 리스너
        binding.btnStop.setOnClickListener {
            stopRecording()
        }

        // 녹음 파일 보기 버튼 클릭 리스너
        binding.btnViewRecordings.setOnClickListener {
            val intent = Intent(this, AudioRecordDataActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkPermissionAndStartRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1001
            )
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile)
            prepare()
            start()
        }

        // UI 상태 업데이트
        isRecording = true
        binding.textRecordingState.text = "녹음 중..."
        binding.progressRecording.visibility = View.VISIBLE
        binding.btnRecord.visibility = View.GONE
        binding.btnStop.visibility = View.VISIBLE
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        // UI 상태 업데이트
        isRecording = false
        binding.textRecordingState.text = "녹음 완료"
        binding.progressRecording.visibility = View.GONE
        binding.btnRecord.visibility = View.VISIBLE
        binding.btnStop.visibility = View.GONE

        Toast.makeText(this, "녹음이 저장되었습니다: $outputFile", Toast.LENGTH_SHORT).show()

        // 녹음 파일 길이 계산
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(outputFile)
        mediaPlayer.prepare()
        val duration = mediaPlayer.duration // 밀리초 단위로 녹음 길이 반환
        mediaPlayer.release()

        // 녹음 파일 정보를 데이터베이스에 저장
        saveRecordingToDatabase(outputFile, formatRecordingDuration(duration))
    }

    private fun formatRecordingDuration(durationMillis: Int): String {
        val minutes = (durationMillis / 1000) / 60
        val seconds = (durationMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    private fun saveRecordingToDatabase(filePath: String, duration: String) {
        val fileName = filePath.substring(filePath.lastIndexOf("/") + 1) // 파일 이름 추출
        val timestamp = System.currentTimeMillis() // 현재 시간 저장

        // 데이터베이스에 저장할 객체 생성
        val audioRecord = com.largeblueberry.aicompose.record.database.AudioRecordEntity(
            filename = fileName,
            filePath = filePath,
            timestamp = timestamp,
            duration = duration
        )

        // 코루틴을 사용하여 데이터베이스에 저장
        lifecycleScope.launch {
            try {
                database.audioRecordDao().insertRecord(audioRecord)
                Toast.makeText(
                    this@RecordActivity,
                    "녹음 파일이 데이터베이스에 저장되었습니다: $fileName",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@RecordActivity,
                    "데이터베이스 저장 중 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        } else {
            Toast.makeText(this, "녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
}