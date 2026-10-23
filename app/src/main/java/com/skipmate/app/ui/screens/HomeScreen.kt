package com.skipmate.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skipmate.app.R
import com.skipmate.app.data.diagnostics.DiagnosticEvent
import com.skipmate.app.data.diagnostics.DiagnosticStatus
import com.skipmate.app.platform.AppInstallStatus
import com.skipmate.app.platform.TargetAppInfo
import com.skipmate.app.ui.components.SkipMateSwitch
import com.skipmate.app.ui.components.StatusCard
import com.skipmate.app.ui.theme.Primary
import com.skipmate.app.ui.theme.PrimaryContainer
import com.skipmate.app.ui.theme.StatusError
import com.skipmate.app.ui.theme.StatusNeutral
import com.skipmate.app.ui.theme.StatusSuccess
import com.skipmate.app.ui.theme.StatusWarning

@Composable
fun HomeScreen(
    isAutoSkipEnabled: Boolean,
    isAccessibilityGranted: Boolean,
    targetAppInfo: TargetAppInfo,
    latestDiagnosticEvent: DiagnosticEvent?,
    lastSuccessfulSkip: DiagnosticEvent? = null,
    totalSkipsCount: Int = 0,
    onToggleAutoSkip: (Boolean) -> Unit,
    onConfigurePermissions: () -> Unit,
    onOpenYouTube: () -> Unit,
    onOpenDiagnostics: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenSubscription: () -> Unit = {},
    onOpenTrial: () -> Unit = onOpenSubscription,
    userProfile: com.skipmate.app.data.user.UserProfile? = null
) {
    val isPaidPro = userProfile?.subscriptionStatus.equals("active", ignoreCase = true)
    val isTrial = userProfile?.subscriptionStatus.equals("trial", ignoreCase = true)
    val isPro = isPaidPro || isTrial

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_skipmate_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (isPro) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusSuccess.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isTrial) "★ TRIAL" else "★ PRO",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = StatusSuccess
                                )
                            }
                        }
                    }
                    Text(
                        text = if (userProfile != null && userProfile.name.isNotBlank()) {
                            val accountTier = if (isTrial) {
                                "7-Day Free Trial Active"
                            } else if (isPaidPro) {
                                "Pro Member"
                            } else {
                                "Free Account"
                            }
                            "${userProfile.name} • $accountTier"
                        } else {
                            stringResource(id = R.string.app_subtitle)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPro) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // SkipMate Pro Feature Banner (Only shown when not paid Pro; hidden once Pro is purchased)
            if (!isPaidPro) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTrial) "SkipMate Pro • Trial Active" else "SkipMate Pro",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isTrial) {
                                        "7-Day free trial active • Upgrade to claim 25% welcome offer"
                                    } else {
                                        "Instant ad skipping • Choose your activation below"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Option 1: Welcome Offer (25% OFF Yearly Plan) -> redirects to subscription plan
                            OutlinedButton(
                                onClick = onOpenSubscription,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, Color(0xFFEC4899)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEC4899)),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "⚡ Welcome Offer",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Option 2: Start 7-Days Free Trial -> redirects to trial plan
                            Button(
                                onClick = onOpenTrial,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isTrial) "🎉 Trial Active" else "🎉 Start 7-Days",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Main Automatic Skip Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.card_automatic_skip_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.card_automatic_skip_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        SkipMateSwitch(
                            checked = isAutoSkipEnabled,
                            onCheckedChange = onToggleAutoSkip,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contextual status description
                    val dynamicStatusDesc = when {
                        !isAutoSkipEnabled -> stringResource(id = R.string.desc_auto_skip_disabled)
                        !isAccessibilityGranted -> stringResource(id = R.string.desc_permission_missing)
                        else -> stringResource(id = R.string.desc_service_active)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = dynamicStatusDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (!isAutoSkipEnabled) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else if (!isAccessibilityGranted) {
                                StatusWarning
                            } else {
                                StatusSuccess
                            }
                        )
                    }
                }
            }

            // YouTube Detection Status Card
            val (ytStatusText, ytStatusColor) = when (targetAppInfo.status) {
                AppInstallStatus.INSTALLED -> "Installed" to StatusSuccess
                AppInstallStatus.DISABLED -> "Disabled in OS" to StatusWarning
                AppInstallStatus.NOT_INSTALLED -> "Not Installed" to StatusError
            }

            StatusCard(
                title = "YouTube Status",
                statusText = ytStatusText,
                statusColor = ytStatusColor,
                description = if (targetAppInfo.versionName != null) {
                    "Detected official YouTube v${targetAppInfo.versionName}"
                } else {
                    "YouTube app not found on this device."
                },
                icon = Icons.Default.PlayArrow
            )

            // Skip Assistant Readiness Status Card
            val (asstStatusText, asstStatusColor, asstDesc) = when {
                !isAccessibilityGranted -> Triple(
                    "Permission Required",
                    StatusWarning,
                    "Android Accessibility permission is required to detect skip buttons."
                )
                !isAutoSkipEnabled -> Triple(
                    "Detection Paused",
                    StatusNeutral,
                    "Automatic skipping is paused via the master toggle above."
                )
                targetAppInfo.status != AppInstallStatus.INSTALLED -> Triple(
                    "Target Missing",
                    StatusError,
                    "YouTube must be installed to monitor skip controls."
                )
                else -> Triple(
                    "Ready",
                    StatusSuccess,
                    "Actively watching for official YouTube Skip Ads buttons."
                )
            }

            StatusCard(
                title = "Skip Assistant",
                statusText = asstStatusText,
                statusColor = asstStatusColor,
                description = asstDesc,
                icon = Icons.Default.Shield
            )

            // Last Activity Status Card
            val activityInfo = when {
                !isAccessibilityGranted -> ActivityCardInfo(
                    statusText = "Permission Required",
                    color = StatusWarning,
                    icon = Icons.Default.Warning,
                    line1 = "Accessibility Permission Required",
                    line2 = "Enable SkipMate in Android Accessibility settings to activate auto-skip."
                )
                !isAutoSkipEnabled -> ActivityCardInfo(
                    statusText = "Detection Paused",
                    color = StatusNeutral,
                    icon = Icons.Default.Visibility,
                    line1 = "Automatic Skip Paused",
                    line2 = "Turn on the Automatic Skip switch above to resume."
                )
                lastSuccessfulSkip != null -> {
                    val timeStr = try {
                        lastSuccessfulSkip.formattedTime.substringBeforeLast(".")
                    } catch (e: Exception) {
                        lastSuccessfulSkip.formattedTime
                    }
                    ActivityCardInfo(
                        statusText = "Ad Skipped Automatically",
                        color = StatusSuccess,
                        icon = Icons.Default.CheckCircle,
                        line1 = "✔ Ad Skipped Automatically",
                        line2 = "Total Ads Skipped: $totalSkipsCount • Last skipped at $timeStr"
                    )
                }
                latestDiagnosticEvent?.status == DiagnosticStatus.CLICK_ATTEMPTED -> ActivityCardInfo(
                    statusText = "Skip In Progress",
                    color = Primary,
                    icon = Icons.Default.CheckCircle,
                    line1 = "Attempting to click Skip Ads button...",
                    line2 = "Interacting with detected YouTube skip control."
                )
                latestDiagnosticEvent?.status == DiagnosticStatus.CLICK_DISPATCH_FAILED -> ActivityCardInfo(
                    statusText = "Interaction Unconfirmed",
                    color = StatusWarning,
                    icon = Icons.Default.Warning,
                    line1 = "Click action could not be confirmed",
                    line2 = "YouTube button was no longer available or framework rejected action."
                )
                else -> ActivityCardInfo(
                    statusText = "Active Monitoring & Ready",
                    color = StatusSuccess,
                    icon = Icons.Default.CheckCircle,
                    line1 = "Ads Skip Service Enabled",
                    line2 = ""
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(activityInfo.color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = activityInfo.icon,
                                contentDescription = null,
                                tint = activityInfo.color,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Last Activity",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(activityInfo.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activityInfo.statusText,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = activityInfo.color
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = activityInfo.line1,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = activityInfo.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onConfigurePermissions,
                    modifier = Modifier
                        .weight(1f)
                        .height(74.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Permissions",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenYouTube,
                    modifier = Modifier
                        .weight(1f)
                        .height(74.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    enabled = targetAppInfo.status == AppInstallStatus.INSTALLED,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = if (targetAppInfo.status == AppInstallStatus.INSTALLED) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "YouTube",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .weight(1f)
                        .height(74.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private data class ActivityCardInfo(
    val statusText: String,
    val color: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val line1: String,
    val line2: String
)
