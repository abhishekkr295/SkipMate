package com.skipmate.app.data.user

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skipmate.app.data.preferences.dataStore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FirestoreUserProfileRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserProfileRepository {

    private object PreferencesKeys {
        val PROFILE_UID = stringPreferencesKey("profile_uid")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_EMAIL = stringPreferencesKey("profile_email")
        val PROFILE_SUB_STATUS = stringPreferencesKey("profile_sub_status")
        val PROFILE_SUB_PLAN = stringPreferencesKey("profile_sub_plan")
        val PROFILE_AUTOPAY = booleanPreferencesKey("profile_autopay_enabled")
        val PROFILE_CREATED_AT = longPreferencesKey("profile_created_at")
        val PROFILE_UPDATED_AT = longPreferencesKey("profile_updated_at")
    }

    override val currentProfile: Flow<UserProfile?> = context.dataStore.data.map { prefs ->
        val uid = prefs[PreferencesKeys.PROFILE_UID]
        if (!uid.isNullOrBlank()) {
            UserProfile(
                uid = uid,
                name = prefs[PreferencesKeys.PROFILE_NAME] ?: "",
                email = prefs[PreferencesKeys.PROFILE_EMAIL] ?: "",
                subscriptionStatus = prefs[PreferencesKeys.PROFILE_SUB_STATUS] ?: "free",
                subscriptionPlan = prefs[PreferencesKeys.PROFILE_SUB_PLAN] ?: "free",
                autoPayEnabled = prefs[PreferencesKeys.PROFILE_AUTOPAY] ?: true,
                createdAt = prefs[PreferencesKeys.PROFILE_CREATED_AT] ?: System.currentTimeMillis(),
                updatedAt = prefs[PreferencesKeys.PROFILE_UPDATED_AT] ?: System.currentTimeMillis()
            )
        } else {
            null
        }
    }.distinctUntilChanged()

    override suspend fun createProfileIfMissing(
        uid: String,
        name: String,
        email: String
    ): Result<UserProfile> = withContext(ioDispatcher) {
        try {
            val existing = getProfile(uid).getOrNull()
            if (existing != null && existing.email.isNotBlank()) {
                return@withContext Result.success(existing)
            }

            val now = System.currentTimeMillis()
            val newProfile = UserProfile(
                uid = uid,
                name = name,
                email = email,
                subscriptionStatus = "free",
                subscriptionPlan = "free",
                autoPayEnabled = true,
                createdAt = now,
                updatedAt = now
            )

            // Cache locally and sync with Firestore
            saveProfileLocally(newProfile)
            syncToFirestoreDoc(newProfile)

            Result.success(newProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProfile(uid: String): Result<UserProfile> = withContext(ioDispatcher) {
        try {
            // Check Firestore doc or fallback to local cache
            val remoteProfile = fetchFromFirestoreDoc(uid)
            val profile = remoteProfile ?: readProfileLocally(uid)

            if (profile != null) {
                saveProfileLocally(profile)
                Result.success(profile)
            } else {
                Result.failure(NoSuchElementException("No profile found for UID: $uid"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePermittedFields(
        uid: String,
        name: String?,
        autoPayEnabled: Boolean?
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            context.dataStore.edit { prefs ->
                if (prefs[PreferencesKeys.PROFILE_UID] == uid) {
                    name?.let { prefs[PreferencesKeys.PROFILE_NAME] = it }
                    autoPayEnabled?.let { prefs[PreferencesKeys.PROFILE_AUTOPAY] = it }
                    prefs[PreferencesKeys.PROFILE_UPDATED_AT] = System.currentTimeMillis()
                }
            }
            // Sync permitted fields to Firestore (subscriptionStatus is explicitly protected)
            updateFirestorePermittedFields(uid, name, autoPayEnabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun activateSubscription(
        uid: String,
        status: String,
        plan: String,
        autoPayEnabled: Boolean
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val now = System.currentTimeMillis()
            context.dataStore.edit { prefs ->
                val currentUid = if (uid.isNotBlank()) uid else (prefs[PreferencesKeys.PROFILE_UID] ?: "local_user")
                prefs[PreferencesKeys.PROFILE_UID] = currentUid
                prefs[PreferencesKeys.PROFILE_SUB_STATUS] = status
                prefs[PreferencesKeys.PROFILE_SUB_PLAN] = plan
                prefs[PreferencesKeys.PROFILE_AUTOPAY] = autoPayEnabled
                prefs[PreferencesKeys.PROFILE_UPDATED_AT] = now
            }

            if (uid.isNotBlank()) {
                try {
                    val firestore = FirebaseFirestore.getInstance()
                    firestore.collection("users").document(uid).update(
                        mapOf(
                            "subscriptionStatus" to status,
                            "subscriptionPlan" to plan,
                            "autoPayEnabled" to autoPayEnabled,
                            "updatedAt" to now
                        )
                    ).await()
                } catch (_: Exception) {
                    // Cache is locally updated even if remote update fails
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearProfileCache() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.PROFILE_UID)
            prefs.remove(PreferencesKeys.PROFILE_NAME)
            prefs.remove(PreferencesKeys.PROFILE_EMAIL)
            prefs.remove(PreferencesKeys.PROFILE_SUB_STATUS)
            prefs.remove(PreferencesKeys.PROFILE_SUB_PLAN)
            prefs.remove(PreferencesKeys.PROFILE_AUTOPAY)
            prefs.remove(PreferencesKeys.PROFILE_CREATED_AT)
            prefs.remove(PreferencesKeys.PROFILE_UPDATED_AT)
        }
    }

    private suspend fun saveProfileLocally(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PROFILE_UID] = profile.uid
            prefs[PreferencesKeys.PROFILE_NAME] = profile.name
            prefs[PreferencesKeys.PROFILE_EMAIL] = profile.email
            prefs[PreferencesKeys.PROFILE_SUB_STATUS] = profile.subscriptionStatus
            prefs[PreferencesKeys.PROFILE_SUB_PLAN] = profile.subscriptionPlan
            prefs[PreferencesKeys.PROFILE_AUTOPAY] = profile.autoPayEnabled
            prefs[PreferencesKeys.PROFILE_CREATED_AT] = profile.createdAt
            prefs[PreferencesKeys.PROFILE_UPDATED_AT] = profile.updatedAt
        }
    }

    private suspend fun readProfileLocally(uid: String): UserProfile? {
        var profile: UserProfile? = null
        context.dataStore.edit { prefs ->
            if (prefs[PreferencesKeys.PROFILE_UID] == uid) {
                profile = UserProfile(
                    uid = uid,
                    name = prefs[PreferencesKeys.PROFILE_NAME] ?: "",
                    email = prefs[PreferencesKeys.PROFILE_EMAIL] ?: "",
                    subscriptionStatus = prefs[PreferencesKeys.PROFILE_SUB_STATUS] ?: "free",
                    subscriptionPlan = prefs[PreferencesKeys.PROFILE_SUB_PLAN] ?: "free",
                    autoPayEnabled = prefs[PreferencesKeys.PROFILE_AUTOPAY] ?: true,
                    createdAt = prefs[PreferencesKeys.PROFILE_CREATED_AT] ?: System.currentTimeMillis(),
                    updatedAt = prefs[PreferencesKeys.PROFILE_UPDATED_AT] ?: System.currentTimeMillis()
                )
            }
        }
        return profile
    }

    // Cloud Firestore integration with local DataStore fallback
    private suspend fun syncToFirestoreDoc(profile: UserProfile) {
        try {
            val db = getFirestoreInstance() ?: return
            val userDoc = hashMapOf<String, Any>(
                "uid" to profile.uid,
                "name" to profile.name,
                "email" to profile.email,
                "subscriptionStatus" to profile.subscriptionStatus,
                "subscriptionPlan" to profile.subscriptionPlan,
                "autoPayEnabled" to profile.autoPayEnabled,
                "createdAt" to profile.createdAt,
                "updatedAt" to profile.updatedAt
            )
            db.collection("users").document(profile.uid).set(userDoc).await()
        } catch (_: Throwable) {
            // Offline fallback: DataStore remains single source of truth
        }
    }

    private suspend fun fetchFromFirestoreDoc(uid: String): UserProfile? {
        return try {
            val db = getFirestoreInstance() ?: return null
            val snapshot = db.collection("users").document(uid).get().await()
            if (snapshot.exists()) {
                UserProfile(
                    uid = snapshot.getString("uid") ?: uid,
                    name = snapshot.getString("name") ?: "",
                    email = snapshot.getString("email") ?: "",
                    subscriptionStatus = snapshot.getString("subscriptionStatus") ?: "free",
                    subscriptionPlan = snapshot.getString("subscriptionPlan") ?: "free",
                    autoPayEnabled = snapshot.getBoolean("autoPayEnabled") ?: true,
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    private suspend fun updateFirestorePermittedFields(uid: String, name: String?, autoPay: Boolean?) {
        try {
            val db = getFirestoreInstance() ?: return
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to System.currentTimeMillis()
            )
            name?.let { updates["name"] = it }
            autoPay?.let { updates["autoPayEnabled"] = it }
            db.collection("users").document(uid).update(updates).await()
        } catch (_: Throwable) {
            // Offline fallback
        }
    }

    private fun getFirestoreInstance(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            null
        }
    }
}
