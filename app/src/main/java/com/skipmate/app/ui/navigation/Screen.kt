package com.skipmate.app.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object PermissionGuide : Screen("permission_guide")
    object Diagnostics : Screen("diagnostics")
    object Settings : Screen("settings")
    object About : Screen("about")
    object Subscription : Screen("subscription?trial={trial}") {
        fun createRoute(trial: Boolean = false) = "subscription?trial=$trial"
    }
}
