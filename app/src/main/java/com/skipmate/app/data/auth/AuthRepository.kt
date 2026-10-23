package com.skipmate.app.data.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    fun getCurrentUserSync(): AuthUser?
    suspend fun signUp(name: String, email: String, password: String): AuthResult<AuthUser>
    suspend fun login(email: String, password: String): AuthResult<AuthUser>
    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit>
    suspend fun sendEmailVerification(): AuthResult<Unit>
    suspend fun logout(): AuthResult<Unit>
}
