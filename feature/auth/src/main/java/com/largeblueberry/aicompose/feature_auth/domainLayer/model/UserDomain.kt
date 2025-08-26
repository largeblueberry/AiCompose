package com.largeblueberry.aicompose.feature_auth.domainLayer.model

data class UserDomain(
    val id: String,
    val name: String,
    val email: String,
    val profilePictureUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * 수정
 */