package com.skipmate.app.platform

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

enum class AppInstallStatus {
    INSTALLED,
    NOT_INSTALLED,
    DISABLED
}

data class TargetAppInfo(
    val packageName: String,
    val appName: String,
    val status: AppInstallStatus,
    val versionName: String? = null
)

class InstalledAppChecker(private val context: Context) {

    fun checkPackage(packageName: String): TargetAppInfo {
        val packageManager = context.packageManager
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }

            val appInfo = packageInfo.applicationInfo
            val isEnabled = appInfo?.enabled ?: false
            val appLabel = appInfo?.let { packageManager.getApplicationLabel(it).toString() } ?: packageName

            TargetAppInfo(
                packageName = packageName,
                appName = appLabel,
                status = if (isEnabled) AppInstallStatus.INSTALLED else AppInstallStatus.DISABLED,
                versionName = packageInfo.versionName
            )
        } catch (e: PackageManager.NameNotFoundException) {
            TargetAppInfo(
                packageName = packageName,
                appName = "YouTube",
                status = AppInstallStatus.NOT_INSTALLED,
                versionName = null
            )
        } catch (e: Exception) {
            TargetAppInfo(
                packageName = packageName,
                appName = "YouTube",
                status = AppInstallStatus.NOT_INSTALLED,
                versionName = null
            )
        }
    }

    fun launchPackage(packageName: String): Boolean {
        val packageManager = context.packageManager
        val launchIntent: Intent? = packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } else {
            false
        }
    }
}
