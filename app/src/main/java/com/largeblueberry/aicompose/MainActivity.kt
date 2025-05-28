package com.largeblueberry.aicompose

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.largeblueberry.aicompose.database.ui.AudioRecordDataActivity
import com.largeblueberry.aicompose.databinding.ActivityMainBinding
import com.largeblueberry.aicompose.record.ui.RecordActivity
import com.largeblueberry.feature_sheetmusic.SheetMusicActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // binding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.page1.setOnClickListener{
            val intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
        }

        binding.page2.setOnClickListener{
            val intent = Intent(this,AudioRecordDataActivity::class.java)
            startActivity(intent)
        }

        binding.page3.setOnClickListener {
            val intent = Intent(this, SheetMusicActivity::class.java)
            startActivity(intent)
        }

        binding.page4.setOnClickListener {
            TODO()
        }

    }
}