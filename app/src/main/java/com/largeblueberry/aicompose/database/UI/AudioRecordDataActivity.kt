package com.largeblueberry.aicompose.database.UI

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
import kotlinx.coroutines.launch
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
                uploadAudioToFirebase(record.filePath) { success, downloadUrlOrError ->
                    if (success) {
                        Toast.makeText(
                            this,
                            "업로드 성공! URL: $downloadUrlOrError",
                            Toast.LENGTH_SHORT
                        ).show()
                        // 공유 인텐트 예시
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, downloadUrlOrError)
                            type = "text/plain"
                        }
                        startActivity(android.content.Intent.createChooser(sendIntent, "공유하기"))
                    } else {
                        Toast.makeText(
                            this,
                            "업로드 실패: $downloadUrlOrError",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AudioRecordDataActivity)
            adapter = this@AudioRecordDataActivity.adapter
        }
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

    private fun uploadAudioToFirebase(filePath: String, onResult: (Boolean, String?) -> Unit) {
        val file = File(filePath)
        if (!file.exists()) {
            onResult(false, "파일이 존재하지 않습니다.")
            return
        }
        val uri = Uri.fromFile(file)
        val storageRef = Firebase.storage.reference
        val audioRef = storageRef.child("audio/${System.currentTimeMillis()}_${file.name}")

        audioRef.putFile(uri)
            .addOnSuccessListener {
                audioRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onResult(true, downloadUri.toString())
                }
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
    }

}