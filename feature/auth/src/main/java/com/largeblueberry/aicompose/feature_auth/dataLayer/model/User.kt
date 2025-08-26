package com.largeblueberry.aicompose.feature_auth.dataLayer.model

import com.google.firebase.Timestamp

data class User(
    val id: String,
    val name: String,
    val email: String,
    val profilePictureUrl: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
)