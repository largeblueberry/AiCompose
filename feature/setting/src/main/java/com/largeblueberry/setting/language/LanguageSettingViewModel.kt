package com.largeblueberry.setting.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.largeblueberry.setting.language.domain.GetLanguageCodeUseCase
import com.largeblueberry.setting.language.domain.SetLanguageCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageSettingViewModel @Inject constructor(
    private val getLanguageCodeUseCase: GetLanguageCodeUseCase,
    private val setLanguageCodeUseCase: SetLanguageCodeUseCase
) : ViewModel() {

    val currentLanguageCode = getLanguageCodeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "ko"
        )

    fun onLanguageSelected(languageCode: String) {
        viewModelScope.launch {
            setLanguageCodeUseCase(languageCode)
        }
    }
}
