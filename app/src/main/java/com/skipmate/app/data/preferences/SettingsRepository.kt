package com.skipmate.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "skipmate_settings")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserSettings(
    val isAutoSkipEnabled: Boolean = true,
    val targetPackageName: String = DEFAULT_TARGET_PACKAGE,
    val isDiagnosticLoggingEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isOnboardingCompleted: Boolean = false,
    val selectedLanguage: String = "en"
) {
    companion object {
        const val DEFAULT_TARGET_PACKAGE = "com.google.android.youtube"
    }
}

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val AUTO_SKIP_ENABLED = booleanPreferencesKey("auto_skip_enabled")
        val TARGET_PACKAGE = stringPreferencesKey("target_package")
        val DIAGNOSTIC_LOGGING_ENABLED = booleanPreferencesKey("diagnostic_logging_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val autoSkip = preferences[PreferencesKeys.AUTO_SKIP_ENABLED] ?: true
            val targetPkg = preferences[PreferencesKeys.TARGET_PACKAGE] ?: UserSettings.DEFAULT_TARGET_PACKAGE
            val diagLogging = preferences[PreferencesKeys.DIAGNOSTIC_LOGGING_ENABLED] ?: true
            val themeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(themeStr)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
            val onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
            val language = preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: "en"

            UserSettings(
                isAutoSkipEnabled = autoSkip,
                targetPackageName = targetPkg,
                isDiagnosticLoggingEnabled = diagLogging,
                themeMode = themeMode,
                isOnboardingCompleted = onboardingCompleted,
                selectedLanguage = language
            )
        }

    suspend fun setAutoSkipEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SKIP_ENABLED] = enabled
        }
    }

    suspend fun setTargetPackageName(packageName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TARGET_PACKAGE] = packageName
        }
    }

    suspend fun setDiagnosticLoggingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DIAGNOSTIC_LOGGING_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setSelectedLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE] = languageCode
        }
    }

    suspend fun resetAllSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
