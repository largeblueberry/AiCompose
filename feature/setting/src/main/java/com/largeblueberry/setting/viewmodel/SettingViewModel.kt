package com.largeblueberry.setting.viewmodel

import androidx.lifecycle.ViewModel
import com.largeblueberry.analyticshelper.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private companion object {
        private const val TAG = "SettingViewModel"
    }
}
