package com.largeblueberry.aicompose.record.ui

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
import com.largeblueberry.aicompose.library.ui.LibraryActivity
import com.largeblueberry.aicompose.databinding.ActivityRecordBinding
import androidx.core.graphics.toColorInt

class RecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordBinding
    private val viewModel: RecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // UI 상태 초기화
        binding.progressRecording.visibility = View.GONE

        // 녹음 시작/중지 토글 버튼
        binding.btnRecordToggle.setOnClickListener {
            if (viewModel.isRecording.value == true) {
                viewModel.stopRecording()
            } else {
                checkPermissionAndStartRecording()
            }
        }

        // 녹음 파일 목록 보기
        binding.btnViewRecordings.setOnClickListener {
            val intent = Intent(this, LibraryActivity::class.java)
            startActivity(intent)
        }

        // ViewModel LiveData 관찰
        viewModel.isRecording.observe(this) { isRecording ->
            if (isRecording) {
                binding.textRecordingState.text = "녹음 중..."
                binding.progressRecording.visibility = View.VISIBLE
                binding.btnRecordToggle.text = "녹음 중지"
                binding.btnRecordToggle.setBackgroundColor("#FF4F4F".toColorInt()) // 빨간색
                binding.btnRecordToggle.setTextColor("#FFFFFF".toColorInt())       // 흰색
            } else {
                binding.textRecordingState.text = "녹음 완료"
                binding.progressRecording.visibility = View.GONE
                binding.btnRecordToggle.text = "녹음 시작"
                binding.btnRecordToggle.setBackgroundColor("#4F8CFF".toColorInt()) // 파란색
                binding.btnRecordToggle.setTextColor("#FFFFFF".toColorInt())       // 흰색
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