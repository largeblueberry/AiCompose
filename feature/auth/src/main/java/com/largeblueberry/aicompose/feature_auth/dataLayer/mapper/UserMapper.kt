package com.largeblueberry.aicompose.feature_auth.dataLayer.mapper

import com.largeblueberry.aicompose.feature_auth.dataLayer.model.User

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.largeblueberry.auth.model.UserCore

object UserMapper {

    fun toDomain(user: User): UserCore {
        return UserCore(
            id = user.id,
            name = user.name,
            email = user.email,
            profilePictureUrl = user.profilePictureUrl,
            createdAt = user.createdAt.toDate().time
        )
    }

    fun toData(userCore: UserCore): User {
        return User(
            id = userCore.id,
            name = userCore.name,
            email = userCore.email,
            profilePictureUrl = userCore.profilePictureUrl,
            // Long (밀리초)을 Timestamp 객체로 변환
            createdAt = Timestamp(java.util.Date(userCore.createdAt))
        )
    }

    // FirebaseUser (Firebase SDK) -> User (Data Layer)
    // FirebaseUser에서 User 데이터 모델을 생성하는 함수
    fun toUser(firebaseUser: FirebaseUser): User {
        // FirebaseUser의 metadata에서 생성 시간을 가져옵니다.
        // metadata가 null이거나 creationTimestamp가 null일 경우 현재 시간으로 대체
        val creationTimeMillis = firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis()

        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: firebaseUser.email?.split("@")?.get(0) ?: "User", // displayName이 없을 경우 이메일 앞부분 사용 또는 기본값
            email = firebaseUser.email ?: "", // 이메일은 대부분 존재하지만, 혹시 모를 경우 빈 문자열
            profilePictureUrl = firebaseUser.photoUrl?.toString(), // URL을 String으로 변환
            // Long (밀리초)을 Timestamp 객체로 변환
            // Timestamp 생성자는 (초, 나노초)를 받으므로 밀리초를 초와 나노초로 분리
            createdAt = Timestamp(creationTimeMillis / 1000, (creationTimeMillis % 1000 * 1_000_000).toInt())
        )
    }



}