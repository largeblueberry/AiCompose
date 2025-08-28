package com.largeblueberry.auth.model

data class UserCore(
    val id: String,
    val name: String,
    val email: String,
    val profilePictureUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)