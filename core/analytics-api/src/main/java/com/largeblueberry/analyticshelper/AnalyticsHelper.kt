package com.largeblueberry.analyticshelper

interface AnalyticsHelper {
    fun logEvent(name: String, params: Map<String, Any>)
    fun setUserId(userId: String?)
    fun recordException(throwable: Throwable)
}
