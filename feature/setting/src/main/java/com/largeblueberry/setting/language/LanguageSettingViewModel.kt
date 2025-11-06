package com.largeblueberry.setting.language

import android.util.Log // 1. Log 임포트
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.analyticshelper.AnalyticsHelper
import com.largeblueberry.domain.repository.LanguageRepository
import com.largeblueberry.setting.language.ui.Language
import com.largeblueberry.setting.language.ui.LanguageUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val languageRepository: LanguageRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    // 2. Logcat 필터링을 위한 TAG 추가
    private companion object {
        private const val TAG = "LanguageViewModel"
    }

    private val _uiState = MutableStateFlow(LanguageUiState())
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    init {
        val availableLanguages = listOf(
            Language("English", "en"),
            Language("한국어", "ko"),
        )

        viewModelScope.launch {
            languageRepository.language.collect { selectedCode ->
                // 3. DataStore에서 언어 코드를 가져올 때 로그 출력
                Log.d(TAG, "Language collected from repository: $selectedCode")
                _uiState.update {
                    it.copy(
                        availableLanguages = availableLanguages,
                        selectedLanguageCode = selectedCode
                    )
                }
            }
        }
    }

    fun onLanguageSelected(languageCode: String) {
        // 4. 언어가 선택되었을 때 로그 출력
        Log.d(TAG, "onLanguageSelected called with code: $languageCode")

        // 사용자가 언어를 선택했을 때, 분석 이벤트를 보냄.
        analyticsHelper.logEvent(
            name = "language_select", // 이벤트 이름 (Firebase 콘솔에서 보게 될 이름)
            params = mapOf("language_code" to languageCode) // 함께 보낼 데이터
        )


        viewModelScope.launch {
            languageRepository.setLanguage(languageCode)
        }
    }
}
