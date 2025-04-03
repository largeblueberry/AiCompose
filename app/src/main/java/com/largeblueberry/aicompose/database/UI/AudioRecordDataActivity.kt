package com.largeblueberry.aicompose.database.UI

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.largeblueberry.aicompose.databinding.ActivityRecordDataBinding
import com.largeblueberry.aicompose.record.UI.AudioPlayer
import kotlinx.coroutines.launch

class AudioRecordDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordDataBinding

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

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
    }

    private fun setupRecyclerView() {
        adapter = AudioRecordAdapter(
            onItemClick = { record ->
                // 녹음 파일 재생 로직
                audioPlayer.play(record.filePath)
            },
            onDeleteClick = { record ->
                viewModel.deleteRecord(record)
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
    }
}

