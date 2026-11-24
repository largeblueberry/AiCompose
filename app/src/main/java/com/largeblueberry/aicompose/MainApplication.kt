package com.largeblueberry.aicompose

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * WorkManager를 Hilt 주입이 되는 설정으로 초기화한다(처음 쓸 때, on-demand).
 * 기본 자동 초기화는 매니페스트에서 뺐다: 남겨 두면 주입이 안 되는 기본 설정이 먼저 돌아
 * Worker 생성이 실패한다(docs/troubleshooting.md B-18).
 */
@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
