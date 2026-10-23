package com.skipmate.app.data.auth

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String = "",
    val isEmailVerified: Boolean = false
)
