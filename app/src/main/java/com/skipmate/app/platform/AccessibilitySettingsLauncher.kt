package com.skipmate.app.platform

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.skipmate.app.accessibility.SkipMateAccessibilityService

class AccessibilitySettingsLauncher(private val context: Context) {

    fun isAccessibilityServiceEnabled(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false

        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedServiceName = ComponentName(context, SkipMateAccessibilityService::class.java).flattenToString()
        val expectedShortName = ComponentName(context, SkipMateAccessibilityService::class.java).flattenToShortString()

        for (service in enabledServices) {
            val serviceId = service.id
            if (serviceId.equals(expectedServiceName, ignoreCase = true) ||
                serviceId.equals(expectedShortName, ignoreCase = true)
            ) {
                return true
            }
        }
        return false
    }

    fun openAccessibilitySettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
