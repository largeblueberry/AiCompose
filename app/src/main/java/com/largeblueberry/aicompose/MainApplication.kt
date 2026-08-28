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

    @Inject lateinit var languageRepository: LanguageRepository

    override fun onCreate() {
        super.onCreate()
        // 앱이 생성될 때 저장된 언어 설정을 불러옵니다.
        setupInitialLanguage()
    }

    private fun setupInitialLanguage() {
        val languageCode = runBlocking {
            languageRepository.language.first()
        }

        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}