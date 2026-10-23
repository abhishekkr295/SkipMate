package com.skipmate.app

import android.app.Application
import com.skipmate.app.data.auth.AuthRepository
import com.skipmate.app.data.auth.FirebaseAuthRepository
import com.skipmate.app.data.billing.SubscriptionRepository
import com.skipmate.app.data.diagnostics.DiagnosticRepository
import com.skipmate.app.data.preferences.SettingsRepository
import com.skipmate.app.data.user.FirestoreUserProfileRepository
import com.skipmate.app.data.user.UserProfileRepository
import com.skipmate.app.platform.AccessibilitySettingsLauncher
import com.skipmate.app.platform.InstalledAppChecker

class SkipMateApplication : Application() {

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var diagnosticRepository: DiagnosticRepository
        private set

    lateinit var installedAppChecker: InstalledAppChecker
        private set

    lateinit var accessibilityLauncher: AccessibilitySettingsLauncher
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var userProfileRepository: UserProfileRepository
        private set

    lateinit var subscriptionRepository: SubscriptionRepository
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
        diagnosticRepository = DiagnosticRepository()
        installedAppChecker = InstalledAppChecker(this)
        accessibilityLauncher = AccessibilitySettingsLauncher(this)

        val firestoreUserRepo = FirestoreUserProfileRepository(this)
        userProfileRepository = firestoreUserRepo
        authRepository = FirebaseAuthRepository(this)
        subscriptionRepository = SubscriptionRepository(firestoreUserRepo)
    }
}
