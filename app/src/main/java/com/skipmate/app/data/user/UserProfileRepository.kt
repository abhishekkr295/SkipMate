package com.skipmate.app.data.user

import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    val currentProfile: Flow<UserProfile?>
    suspend fun createProfileIfMissing(uid: String, name: String, email: String): Result<UserProfile>
    suspend fun getProfile(uid: String): Result<UserProfile>
    suspend fun updatePermittedFields(uid: String, name: String?, autoPayEnabled: Boolean?): Result<Unit>
    suspend fun activateSubscription(uid: String, status: String, plan: String, autoPayEnabled: Boolean = true): Result<Unit>
    suspend fun clearProfileCache()
}
