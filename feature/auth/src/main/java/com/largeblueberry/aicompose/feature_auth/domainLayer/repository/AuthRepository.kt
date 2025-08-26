package com.largeblueberry.aicompose.feature_auth.domainLayer.repository

import com.largeblueberry.aicompose.feature_auth.domainLayer.model.AuthResultDomain

interface AuthRepository {

    suspend fun signIn(idToken: String) : AuthResultDomain

    suspend fun signOut(): Result<Unit>
}


/**
 * signIn: idToken은 보편적으로 String 타입이어서 이렇게 구현함.
 * 여긴 Domain Layer라서 FirebaseUser 같은 구체적인 구현체를 쓰지 않음.
 */