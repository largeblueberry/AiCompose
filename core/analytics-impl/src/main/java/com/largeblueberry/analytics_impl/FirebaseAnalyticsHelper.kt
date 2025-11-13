package com.largeblueberry.analytics_impl

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.largeblueberry.analyticshelper.AnalyticsHelper

/**
 * AnalyticsHelper 인터페이스의 Firebase 구현체.
 * Firebase Analytics와 Crashlytics를 사용하여 실제 로깅 작업을 수행합니다.
 *
 * @property firebaseAnalytics Firebase Analytics 인스턴스. 이벤트 로깅에 사용됩니다.
 * @property firebaseCrashlytics Firebase Crashlytics 인스턴스. 사용자 ID 설정 및 예외 기록에 사용됩니다.
 */
class FirebaseAnalyticsHelper(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : AnalyticsHelper {

    /**
     * Firebase Analytics에 이벤트를 기록합니다.
     * Map으로 받은 파라미터를 Firebase가 요구하는 Bundle 형태로 변환합니다.
     *
     * @param name 이벤트 이름 (예: "screen_view", "button_click")
     * @param params 이벤트와 함께 기록할 추가 정보. Key-Value 쌍.
     */
    override fun logEvent(name: String, params: Map<String, Any>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    // 필요에 따라 다른 타입(e.g., ArrayList)을 추가할 수 있습니다.
                }
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    /**
     * 사용자가 식별되면(예: 로그인 시) 사용자 ID를 설정합니다.
     * 이 ID는 Analytics 리포트와 Crashlytics 크래시 리포트에 연결되어
     * 특정 사용자에게 발생한 이벤트를 필터링하는 데 사용됩니다.
     *
     * @param userId 앱에서 사용하는 고유한 사용자 식별자. 로그아웃 시 null을 전달할 수 있습니다.
     */
    override fun setUserId(userId: String?) {
        val id = userId ?: "" // Crashlytics는 null을 허용하지 않으므로 빈 문자열로 대체
        firebaseAnalytics.setUserId(userId)
        firebaseCrashlytics.setUserId(id)
    }

    /**
     * 처리되지 않은 예외(non-fatal exception)를 Crashlytics에 기록합니다.
     * 앱이 강제 종료되지는 않지만, 개발자가 알아야 할 오류 상황을 리포트할 때 사용합니다.
     *
     * @param throwable 기록할 예외 객체.
     */
    override fun recordException(throwable: Throwable) {
        firebaseCrashlytics.recordException(throwable)
    }
}