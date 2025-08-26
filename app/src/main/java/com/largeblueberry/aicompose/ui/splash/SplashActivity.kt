package com.largeblueberry.aicompose.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.largeblueberry.aicompose.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
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


/**
 * Lint 경고
 *
 * 기존에 개발자들이 Activity를 만들어 delay를 주거나 특정 로딩 작업을 수행한 후 메인 화면으로 넘어가는 방식은 "커스텀 스플래시 화면"으로 간주한다.
 * Android Studio의 Lint 도구는 이러한 커스텀 스플래시 화면 구현을 감지하면,
 * 개발자가 새로운 시스템 스플래시 화면 API를 사용하도록 권장하기 위해 CustomSplashScreen 경고를 발생시킨다.
 *
 * @SuppressLint의 역할
 *
 * @SuppressLint("CustomSplashScreen")
 *  -> "이 부분에서는 CustomSplashScreen과 관련된 Lint 경고를 무시해도 좋다"고 Lint 도구에 알려주는 역할 수행
 * 즉, 개발자가 해당 경고의 의미를 알고 있으며, 의도적으로 커스텀 스플래시 화면 방식을 사용하고 있음을 명시한다.
 */