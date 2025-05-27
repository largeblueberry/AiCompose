package com.largeblueberry.aicompose.database.UI

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.largeblueberry.aicompose.databinding.ActivityRecordDataBinding
import com.largeblueberry.aicompose.record.UI.AudioPlayer
import com.largeblueberry.aicompose.record.database.AudioRecordEntity
import com.largeblueberry.aicompose.retrofit.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class AudioRecordDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordDataBinding

    // ViewModel 초기화 (Factory 이용)
    private val viewModel: AudioRecordViewModel by viewModels {
        AudioRecordViewModelFactory(applicationContext)
    }
    private lateinit var adapter: AudioRecordAdapter

    private val audioPlayer = AudioPlayer() // AudioPlayer 초기화

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityRecordDataBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = AudioRecordAdapter(
            onItemClick = { record ->
                audioPlayer.play(record.filePath)
            },
            onDeleteClick = { record ->
                viewModel.deleteRecord(record)
            },
            onShareClick = { record ->
                viewModel.uploadAudioToServer(record.filePath) { success, urlOrError ->
                    if (success) {
                        Toast.makeText(this, "업로드 성공! URL: $urlOrError", Toast.LENGTH_SHORT).show()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, urlOrError)
                            type = "text/plain"
                        }
                        startActivity(Intent.createChooser(sendIntent, "공유하기"))
                    } else {
                        Toast.makeText(this, "업로드 실패: $urlOrError", Toast.LENGTH_SHORT).show()
                    }
                }
            },// URL 업로드 후 공유하기 url 받아올 것!
            onRenameClick = { record -> // 파일명 클릭 시
                showRenameDialog(record)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AudioRecordDataActivity)
            adapter = this@AudioRecordDataActivity.adapter
        }
    }

    // 파일명 변경 다이얼로그
    private fun showRenameDialog(record: AudioRecordEntity) {
        val editText = android.widget.EditText(this).apply {
            setText(record.filename)
            setSelection(record.filename.length)
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("파일 이름 변경")
            .setView(editText)
            .setPositiveButton("확인") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty() && newName != record.filename) {
                    viewModel.renameRecord(record, newName)
                } else if (newName == record.filename) {
                    Toast.makeText(this, "동일한 이름입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }


    private fun observeViewModel() {
        // 녹음 기록 리스트 관찰
        lifecycleScope.launch {
            viewModel.audioRecords.collect { records ->
                adapter.updateList(records)
            }
        }

        // 데이터가 비어있는지 여부 관찰
        lifecycleScope.launch {
            viewModel.isEmpty.collect { isEmpty ->
                if (isEmpty) {
                    binding.emptyView.visibility = android.view.View.VISIBLE
                    binding.recyclerView.visibility = android.view.View.GONE
                } else {
                    binding.emptyView.visibility = android.view.View.GONE
                    binding.recyclerView.visibility = android.view.View.VISIBLE
                }
            }
        }

        // 삭제 결과 알림 (성공/실패)
        lifecycleScope.launch {
            viewModel.deleteResult.collect { result ->
                result?.let {
                    if (it.isSuccess) {
                        Toast.makeText(
                            this@AudioRecordDataActivity,
                            "삭제 성공",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@AudioRecordDataActivity,
                            it.exceptionOrNull()?.message ?: "삭제 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    viewModel.clearDeleteResult()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
    }

}