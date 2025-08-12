package com.largeblueberry.aicompose

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.largeblueberry.aicompose.library.ui.LibraryActivity
import com.largeblueberry.aicompose.databinding.ActivityMainBinding
import com.largeblueberry.aicompose.record.ui.RecordActivity
import com.largeblueberry.feature_setting.ui.setting.SettingsActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // binding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardRecord.setOnClickListener{
            val intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
        }

        binding.cardMyWorks.setOnClickListener{
            val intent = Intent(this,LibraryActivity::class.java)
            startActivity(intent)
        }

        binding.cardSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }
}