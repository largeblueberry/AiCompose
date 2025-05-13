package com.largeblueberry.aicompose.database.UI


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.largeblueberry.aicompose.databinding.DatabaseitemBinding
import com.largeblueberry.aicompose.record.database.AudioRecordEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// AudioRecordAdapter.kt
class AudioRecordAdapter(
    private val onItemClick: (AudioRecordEntity) -> Unit,
    private val onDeleteClick: (AudioRecordEntity) -> Unit,
    private val onShareClick: (AudioRecordEntity) -> Unit,
    private val onRenameClick: (AudioRecordEntity) -> Unit // 추가!
) : RecyclerView.Adapter<AudioRecordAdapter.AudioViewHolder>() {

    private var records = emptyList<AudioRecordEntity>()

    inner class AudioViewHolder(private val binding: DatabaseitemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: AudioRecordEntity) {
            binding.apply {
                FileName.text = record.filename
                Duration.text = record.duration

                // 날짜 포맷팅
                val date = Date(record.timestamp)
                val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                RecordDate.text = dateFormat.format(date)

                // 삭제 버튼 클릭 리스너
                btnDelete.setOnClickListener {
                    onDeleteClick(record)
                }

                btnShare.setOnClickListener{
                    onShareClick(record)
                }

                // 아이템 클릭 리스너
                btnPlay.setOnClickListener {
                    onItemClick(record)
                }

                //이름 변경 가능
                FileName.setOnClickListener {
                    onRenameClick(record)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = DatabaseitemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount() = records.size

    fun updateList(newList: List<AudioRecordEntity>) {
        records = newList
        notifyDataSetChanged()
    }

}