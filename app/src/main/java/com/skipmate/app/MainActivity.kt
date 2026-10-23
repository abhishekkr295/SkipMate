package com.skipmate.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.skipmate.app.data.preferences.UserSettings
import com.skipmate.app.ui.navigation.SkipMateNavHost
import com.skipmate.app.ui.theme.SkipMateTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var isAccessibilityGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as SkipMateApplication

        setContent {
            val userSettings by app.settingsRepository.userSettingsFlow.collectAsState(initial = UserSettings())

            val langCode = userSettings.selectedLanguage.ifBlank { "en" }
            val currentLocale = remember(langCode) { Locale(langCode) }
            val context = LocalContext.current

            val localizedContext = remember(langCode, context) {
                Locale.setDefault(currentLocale)
                val config = Configuration(context.resources.configuration).apply {
                    setLocale(currentLocale)
                    setLayoutDirection(currentLocale)
                }
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                context.createConfigurationContext(config)
            }

            val localizedConfiguration = remember(langCode) {
                Configuration(context.resources.configuration).apply {
                    setLocale(currentLocale)
                    setLayoutDirection(currentLocale)
                }
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration
            ) {
                SkipMateTheme(themeMode = userSettings.themeMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        SkipMateNavHost(
                            app = app,
                            isAccessibilityGranted = isAccessibilityGranted
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val app = application as? SkipMateApplication ?: return
        isAccessibilityGranted = app.accessibilityLauncher.isAccessibilityServiceEnabled()
    }
}
