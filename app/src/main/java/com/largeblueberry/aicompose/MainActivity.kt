package com.largeblueberry.aicompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.largeblueberry.aicompose.database.UI.AudioRecordDataActivity
import com.largeblueberry.aicompose.databinding.ActivityMainBinding
import com.largeblueberry.aicompose.record.RecordActivity

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    }
}