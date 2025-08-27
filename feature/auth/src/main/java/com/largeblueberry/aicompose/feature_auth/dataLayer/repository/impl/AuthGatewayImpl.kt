package com.largeblueberry.aicompose.feature_auth.dataLayer.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.largeblueberry.usertracker.repository.AuthGateway
import javax.inject.Inject

class AuthGatewayImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthGateway {

    private val LOGGED_IN_MAX_UPLOADS = 5
    private val ANONYMOUS_MAX_UPLOADS = 1

    override suspend fun isLoggedIn(): Boolean {

        return firebaseAuth.currentUser != null
    }

    override suspend fun getCurrentUserId(): String? {

        return firebaseAuth.currentUser?.uid
    }

    override suspend fun getUploadLimitForUser(userId: String?): Int {

        return if (userId != null) {
            LOGGED_IN_MAX_UPLOADS
        } else {
            ANONYMOUS_MAX_UPLOADS
        }
    }
}