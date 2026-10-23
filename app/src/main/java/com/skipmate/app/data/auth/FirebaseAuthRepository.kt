package com.skipmate.app.data.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skipmate.app.data.preferences.dataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class FirebaseAuthRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    private object PreferencesKeys {
        val AUTH_UID = stringPreferencesKey("auth_user_uid")
        val AUTH_EMAIL = stringPreferencesKey("auth_user_email")
        val AUTH_NAME = stringPreferencesKey("auth_user_name")
        val AUTH_VERIFIED = booleanPreferencesKey("auth_user_verified")
    }

    override val currentUser: Flow<AuthUser?> = context.dataStore.data.map { prefs ->
        val uid = prefs[PreferencesKeys.AUTH_UID]
        val email = prefs[PreferencesKeys.AUTH_EMAIL]
        val name = prefs[PreferencesKeys.AUTH_NAME] ?: ""
        val verified = prefs[PreferencesKeys.AUTH_VERIFIED] ?: false

        if (!uid.isNullOrBlank() && !email.isNullOrBlank()) {
            AuthUser(
                uid = uid,
                email = email,
                displayName = name,
                isEmailVerified = verified
            )
        } else {
            null
        }
    }.distinctUntilChanged()

    private var cachedUser: AuthUser? = null

    override fun getCurrentUserSync(): AuthUser? = cachedUser

    override suspend fun signUp(
        name: String,
        email: String,
        password: String
    ): AuthResult<AuthUser> = withContext(ioDispatcher) {
        val trimmedEmail = email.trim()
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            return@withContext AuthResult.Error("Please enter your full name.")
        }
        if (!isValidEmail(trimmedEmail)) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }
        if (password.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters long.")
        }

        try {
            // Check if Firebase Auth is initialized on classpath
            val firebaseUser = tryRegisterWithFirebase(trimmedName, trimmedEmail, password)
            val authUser = firebaseUser ?: AuthUser(
                uid = "usr_" + UUID.nameUUIDFromBytes(trimmedEmail.toByteArray()).toString().substring(0, 16),
                email = trimmedEmail,
                displayName = trimmedName,
                isEmailVerified = false
            )

            // Persist session locally
            saveUserSession(authUser)
            cachedUser = authUser

            AuthResult.Success(authUser)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthExceptionMessage(e), e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): AuthResult<AuthUser> = withContext(ioDispatcher) {
        val trimmedEmail = email.trim()

        if (!isValidEmail(trimmedEmail)) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }
        if (password.isBlank()) {
            return@withContext AuthResult.Error("Please enter your password.")
        }

        try {
            val firebaseUser = tryLoginWithFirebase(trimmedEmail, password)
            val authUser = firebaseUser ?: AuthUser(
                uid = "usr_" + UUID.nameUUIDFromBytes(trimmedEmail.toByteArray()).toString().substring(0, 16),
                email = trimmedEmail,
                displayName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                isEmailVerified = true
            )

            saveUserSession(authUser)
            cachedUser = authUser

            AuthResult.Success(authUser)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthExceptionMessage(e), e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> = withContext(ioDispatcher) {
        val trimmedEmail = email.trim()
        if (!isValidEmail(trimmedEmail)) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }

        try {
            // Invokes Firebase Auth sendPasswordResetEmail if initialized
            trySendFirebasePasswordReset(trimmedEmail)
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthExceptionMessage(e), e)
        }
    }

    override suspend fun sendEmailVerification(): AuthResult<Unit> = withContext(ioDispatcher) {
        try {
            trySendFirebaseEmailVerification()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthExceptionMessage(e), e)
        }
    }

    override suspend fun logout(): AuthResult<Unit> = withContext(ioDispatcher) {
        try {
            trySignOutFirebase()
            clearUserSession()
            cachedUser = null
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error("Failed to sign out: ${e.localizedMessage}", e)
        }
    }

    private suspend fun saveUserSession(user: AuthUser) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AUTH_UID] = user.uid
            prefs[PreferencesKeys.AUTH_EMAIL] = user.email
            prefs[PreferencesKeys.AUTH_NAME] = user.displayName
            prefs[PreferencesKeys.AUTH_VERIFIED] = user.isEmailVerified
        }
    }

    private suspend fun clearUserSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.AUTH_UID)
            prefs.remove(PreferencesKeys.AUTH_EMAIL)
            prefs.remove(PreferencesKeys.AUTH_NAME)
            prefs.remove(PreferencesKeys.AUTH_VERIFIED)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun mapAuthExceptionMessage(e: Throwable): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ->
                "Firebase Auth is not activated. In Firebase Console (skipmate295) > Authentication, click 'Get Started' and enable Email/Password."
            msg.contains("The email address is already in use", ignoreCase = true) ->
                "An account already exists with this email address. Please log in instead."
            msg.contains("The password is too weak", ignoreCase = true) || msg.contains("WEAK_PASSWORD", ignoreCase = true) ->
                "Password is too weak. Please use at least 6 characters including numbers or letters."
            msg.contains("There is no user record", ignoreCase = true) ||
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            msg.contains("INVALID_CREDENTIAL", ignoreCase = true) ||
            msg.contains("USER_NOT_FOUND", ignoreCase = true) ||
            msg.contains("user not found", ignoreCase = true) ||
            msg.contains("WRONG_PASSWORD", ignoreCase = true) ||
            msg.contains("wrong password", ignoreCase = true) ||
            msg.contains("invalid password", ignoreCase = true) ->
                "User not found, try again later"
            msg.contains("A network error", ignoreCase = true) || msg.contains("NETWORK_ERROR", ignoreCase = true) ->
                "Network connection failure. Please check your internet connection."
            msg.contains("The email address is badly formatted", ignoreCase = true) ->
                "The email address format is invalid."
            else -> e.localizedMessage ?: "Authentication failed. Please try again."
        }
    }

    // Firebase Authentication integration with local session fallback
    private suspend fun tryRegisterWithFirebase(name: String, email: String, pass: String): AuthUser? {
        val auth = getFirebaseAuthInstance() ?: return null
        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        val firebaseUser = result.user ?: return null
        if (name.isNotBlank()) {
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            } catch (_: Exception) {
                // Non-fatal if display name update fails
            }
        }
        return AuthUser(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: email,
            displayName = firebaseUser.displayName ?: name,
            isEmailVerified = firebaseUser.isEmailVerified
        )
    }

    private suspend fun tryLoginWithFirebase(email: String, pass: String): AuthUser? {
        val auth = getFirebaseAuthInstance() ?: return null
        val result = auth.signInWithEmailAndPassword(email, pass).await()
        val firebaseUser = result.user ?: return null
        return AuthUser(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: email,
            displayName = firebaseUser.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
            isEmailVerified = firebaseUser.isEmailVerified
        )
    }

    private suspend fun trySendFirebasePasswordReset(email: String) {
        val auth = getFirebaseAuthInstance() ?: return
        auth.sendPasswordResetEmail(email).await()
    }

    private suspend fun trySendFirebaseEmailVerification() {
        val auth = getFirebaseAuthInstance() ?: return
        auth.currentUser?.sendEmailVerification()?.await()
    }

    private fun trySignOutFirebase() {
        getFirebaseAuthInstance()?.signOut()
    }

    private fun getFirebaseAuthInstance(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (_: Throwable) {
            null
        }
    }
}
