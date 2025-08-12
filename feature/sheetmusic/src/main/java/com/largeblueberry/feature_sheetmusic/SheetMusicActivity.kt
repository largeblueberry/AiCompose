package com.largeblueberry.feature_sheetmusic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.largeblueberry.feature_sheetmusic.databinding.ActivitySheetMusicBinding

class SheetMusicActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySheetMusicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySheetMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}