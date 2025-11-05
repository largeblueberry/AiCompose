package com.largeblueberry.aicompose

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.largeblueberry.domain.repository.LanguageRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    // Repository를 주입받습니다.
    @Inject lateinit var languageRepository: LanguageRepository

    override fun onCreate() {
        super.onCreate()
        // 앱이 생성될 때 저장된 언어 설정을 불러옵니다.
        setupInitialLanguage()
    }

    private fun setupInitialLanguage() {
        // runBlocking은 여기서 사용하기에 적합합니다.
        // UI가 그려지기 전에 언어 설정이 동기적으로 완료되어야 하기 때문입니다.
        val languageCode = runBlocking {
            languageRepository.language.first()
        }

        // 저장된 언어 코드로 앱의 로케일을 설정합니다.
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}