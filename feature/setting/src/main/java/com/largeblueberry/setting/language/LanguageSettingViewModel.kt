package com.largeblueberry.setting.language

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.setting.language.domain.GetLanguageCodeUseCase
import com.largeblueberry.setting.language.domain.SetLanguageCodeUseCase
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
    private val getLanguageCodeUseCase: GetLanguageCodeUseCase,
    private val setLanguageCodeUseCase: SetLanguageCodeUseCase
) : ViewModel() {

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
            getLanguageCodeUseCase().collect { selectedCode ->
                Log.d(TAG, "Language collected: $selectedCode")
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
        Log.d(TAG, "onLanguageSelected called with code: $languageCode")
        viewModelScope.launch {
            setLanguageCodeUseCase(languageCode)
        }
    }
}