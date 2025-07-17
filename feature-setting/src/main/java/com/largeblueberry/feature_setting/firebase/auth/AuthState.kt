package com.largeblueberry.feature_setting.firebase.auth

import com.google.firebase.auth.FirebaseUser

sealed class AuthState {
    object Loading : AuthState()
    object NotAuthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

data class UsageLimitResult(
    val canUse: Boolean,
    val remainingCount: Int,
    val dailyLimit: Int,
    val message: String
)

/**
 * UsageLimitResult는 사용자의 일일 사용량 제한을 나타내는 데이터 클래스입니다.
 * sealed class로 정의되어 있으며, 사용자가 기능을 사용할 수 있는지 여부와 남은 사용 횟수, 일일 제한, 메시지를 포함합니다.
 * 사용자가 기능을 사용할 수 있는 경우 canUse는 true이고, 남은 사용 횟수와 일일 제한이 포함됩니다.
 * 사용자가 기능을 사용할 수 없는 경우 canUse는 false이고, 메시지에 제한 초과 등의 이유가 포함됩니다.
 *
 * sealed class는 상속을 제한하는 클래스입니다.
 * 즉, 미리 정의된 하위 클래스들만 상속할 수 있고, 다른 곳에서는 새로운 하위 클래스를 만들 수 없습니다.
 */