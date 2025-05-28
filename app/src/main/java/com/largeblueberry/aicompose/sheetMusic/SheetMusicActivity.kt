package com.largeblueberry.aicompose.sheetMusic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.largeblueberry.aicompose.databinding.ActivitySheetMusicBinding

class SheetMusicActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySheetMusicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySheetMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}