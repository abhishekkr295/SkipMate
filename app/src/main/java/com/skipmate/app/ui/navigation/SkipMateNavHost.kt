package com.skipmate.app.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skipmate.app.SkipMateApplication
import com.skipmate.app.data.preferences.UserSettings
import com.skipmate.app.ui.screens.AboutScreen
import com.skipmate.app.ui.screens.DiagnosticsScreen
import com.skipmate.app.ui.screens.HomeScreen
import com.skipmate.app.ui.screens.LoginScreen
import com.skipmate.app.ui.screens.OnboardingScreen
import com.skipmate.app.ui.screens.PermissionScreen
import com.skipmate.app.ui.screens.SettingsScreen
import com.skipmate.app.ui.screens.SignUpScreen
import com.skipmate.app.ui.screens.SubscriptionScreen
import kotlinx.coroutines.launch

@Composable
fun SkipMateNavHost(
    app: SkipMateApplication,
    isAccessibilityGranted: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val coroutineScope = rememberCoroutineScope()
    val settingsRepo = app.settingsRepository
    val diagRepo = app.diagnosticRepository
    val appChecker = app.installedAppChecker
    val accessLauncher = app.accessibilityLauncher
    val authRepo = app.authRepository
    val userProfileRepo = app.userProfileRepository

    val userSettings by settingsRepo.userSettingsFlow.collectAsState(initial = UserSettings())
    val diagnosticEvents by diagRepo.events.collectAsState()
    val latestDiagEvent by diagRepo.latestEvent.collectAsState()
    val lastSuccessfulSkip by diagRepo.lastSuccessfulSkip.collectAsState()
    val totalSkipsCount by diagRepo.totalSkipsCount.collectAsState()

    val authUser by authRepo.currentUser.collectAsState(initial = authRepo.getCurrentUserSync())
    val userProfile by userProfileRepo.currentProfile.collectAsState(initial = null)

    val targetAppInfo = appChecker.checkPackage(userSettings.targetPackageName)

    // Navigation flow:
    // If onboarding completed:
    //   If logged in -> Home
    //   If not logged in -> Login
    // Else -> Onboarding
    val startDestination = if (userSettings.isOnboardingCompleted) {
        if (authUser != null) Screen.Home.route else Screen.Login.route
    } else {
        Screen.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    coroutineScope.launch {
                        settingsRepo.setOnboardingCompleted(true)
                    }
                    val destination = if (authUser != null) {
                        if (!isAccessibilityGranted) Screen.PermissionGuide.route else Screen.Home.route
                    } else {
                        Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authRepository = authRepo,
                userProfileRepository = userProfileRepo,
                onLoginSuccess = {
                    val nextRoute = if (!isAccessibilityGranted) Screen.PermissionGuide.route else Screen.Home.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                authRepository = authRepo,
                userProfileRepository = userProfileRepo,
                onSignUpSuccess = {
                    val nextRoute = if (!isAccessibilityGranted) Screen.PermissionGuide.route else Screen.Home.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                isAutoSkipEnabled = userSettings.isAutoSkipEnabled,
                isAccessibilityGranted = isAccessibilityGranted,
                targetAppInfo = targetAppInfo,
                latestDiagnosticEvent = latestDiagEvent,
                lastSuccessfulSkip = lastSuccessfulSkip,
                totalSkipsCount = totalSkipsCount,
                userProfile = userProfile,
                onToggleAutoSkip = { enabled ->
                    coroutineScope.launch {
                        settingsRepo.setAutoSkipEnabled(enabled)
                    }
                },
                onConfigurePermissions = {
                    navController.navigate(Screen.PermissionGuide.route)
                },
                onOpenYouTube = {
                    appChecker.launchPackage(userSettings.targetPackageName)
                },
                onOpenDiagnostics = {
                    navController.navigate(Screen.Diagnostics.route)
                },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onOpenSubscription = {
                    navController.navigate(Screen.Subscription.createRoute(trial = false))
                },
                onOpenTrial = {
                    navController.navigate(Screen.Subscription.createRoute(trial = true))
                }
            )
        }

        composable(Screen.PermissionGuide.route) {
            PermissionScreen(
                isPermissionGranted = isAccessibilityGranted,
                onOpenAccessibilitySettings = {
                    accessLauncher.openAccessibilitySettings()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Diagnostics.route) {
            DiagnosticsScreen(
                events = diagnosticEvents,
                isAccessibilityEnabled = isAccessibilityGranted,
                isYouTubeInstalled = targetAppInfo.status == com.skipmate.app.platform.AppInstallStatus.INSTALLED,
                isLoggingEnabled = userSettings.isDiagnosticLoggingEnabled,
                onClearLogs = {
                    diagRepo.clearEvents()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            val context = LocalContext.current
            SettingsScreen(
                userSettings = userSettings,
                onToggleAutoSkip = { enabled ->
                    coroutineScope.launch { settingsRepo.setAutoSkipEnabled(enabled) }
                },
                onToggleDiagnosticLogging = { enabled ->
                    coroutineScope.launch { settingsRepo.setDiagnosticLoggingEnabled(enabled) }
                },
                onSelectThemeMode = { mode ->
                    coroutineScope.launch { settingsRepo.setThemeMode(mode) }
                },
                onSelectLanguage = { lang ->
                    coroutineScope.launch { settingsRepo.setSelectedLanguage(lang) }
                },
                onNavigateToSubscription = {
                    navController.navigate(Screen.Subscription.createRoute(trial = false))
                },
                onRateOnPlayStore = {
                    val packageName = context.packageName
                    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(marketIntent)
                    } catch (e: Exception) {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                    }
                },
                onResetAndLogout = {
                    coroutineScope.launch {
                        authRepo.logout()
                        userProfileRepo.clearProfileCache()
                        settingsRepo.resetAllSettings()
                        diagRepo.clearEvents()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onOpenAccessibilitySettings = {
                    accessLauncher.openAccessibilitySettings()
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.About.route) {
            val context = LocalContext.current
            AboutScreen(
                onRateOnPlayStore = {
                    val packageName = context.packageName
                    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(marketIntent)
                    } catch (e: Exception) {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Subscription.route,
            arguments = listOf(
                navArgument("trial") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isInitialTrial = backStackEntry.arguments?.getBoolean("trial") ?: false
            SubscriptionScreen(
                userProfileRepository = userProfileRepo,
                currentUid = authUser?.uid ?: "",
                initialTrialMode = isInitialTrial,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
