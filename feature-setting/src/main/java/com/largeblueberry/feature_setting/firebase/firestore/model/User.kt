package com.largeblueberry.feature_setting.firebase.firestore.model

import com.google.firebase.Timestamp

data class User(
    val id: String,
    val name: String,
    val email: String,
    val profilePictureUrl: String? = null,
    val isPremium: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val lastLoginAt: Timestamp = Timestamp.now()
)


