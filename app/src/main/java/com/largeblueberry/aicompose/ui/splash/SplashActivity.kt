package com.largeblueberry.aicompose.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.largeblueberry.aicompose.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen()
        }

        // 2초 후 MainActivity로 이동
        lifecycleScope.launch {
            delay(2000) // 2초
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}