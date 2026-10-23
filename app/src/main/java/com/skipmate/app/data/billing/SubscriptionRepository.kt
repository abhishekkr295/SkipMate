package com.skipmate.app.data.billing

import com.skipmate.app.data.user.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubscriptionRepository(
    private val userProfileRepository: UserProfileRepository
) {
    val isProUser: Flow<Boolean> = userProfileRepository.currentProfile.map { profile ->
        profile?.isProActive() ?: false
    }

    val currentStatus: Flow<SubscriptionStatus> = userProfileRepository.currentProfile.map { profile ->
        SubscriptionStatus.fromString(profile?.subscriptionStatus)
    }

    val isAutoPayEnabled: Flow<Boolean> = userProfileRepository.currentProfile.map { profile ->
        profile?.autoPayEnabled ?: true
    }

    suspend fun setAutoPay(uid: String, enabled: Boolean): Result<Unit> {
        return userProfileRepository.updatePermittedFields(uid, name = null, autoPayEnabled = enabled)
    }

    suspend fun syncSubscription(uid: String): Result<Unit> {
        return userProfileRepository.getProfile(uid).map { }
    }
}
