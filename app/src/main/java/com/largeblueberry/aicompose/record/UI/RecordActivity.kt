package com.largeblueberry.aicompose.record.UI

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.largeblueberry.aicompose.database.UI.AudioRecordDataActivity
import com.largeblueberry.aicompose.databinding.ActivityRecordBinding

class RecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordBinding
    private val viewModel: RecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // UI 상태 초기화
        binding.btnStop.visibility = View.GONE
        binding.progressRecording.visibility = View.GONE

        // 권한 체크 후 녹음 시작
        binding.btnRecord.setOnClickListener {
            checkPermissionAndStartRecording()
        }

        // 녹음 중지
        binding.btnStop.setOnClickListener {
            viewModel.stopRecording()
        }

        // 녹음 파일 목록 보기
        binding.btnViewRecordings.setOnClickListener {
            val intent = Intent(this, AudioRecordDataActivity::class.java)
            startActivity(intent)
        }

        // ViewModel LiveData 관찰
        viewModel.isRecording.observe(this) { isRecording ->
            if (isRecording) {
                binding.textRecordingState.text = "녹음 중..."
                binding.progressRecording.visibility = View.VISIBLE
                binding.btnRecord.visibility = View.GONE
                binding.btnStop.visibility = View.VISIBLE
            } else {
                binding.textRecordingState.text = "녹음 완료"
                binding.progressRecording.visibility = View.GONE
                binding.btnRecord.visibility = View.VISIBLE
                binding.btnStop.visibility = View.GONE
            }
        }

        viewModel.recordingStateText.observe(this) { text ->
            binding.textRecordingState.text = text
        }

        viewModel.lastSavedFileName.observe(this) { fileName ->
            if (!fileName.isNullOrBlank()) {
                Toast.makeText(
                    this,
                    "녹음 파일이 저장되었습니다: $fileName",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
            viewModel.startRecording()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.startRecording()
        } else {
            Toast.makeText(this, "녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
}