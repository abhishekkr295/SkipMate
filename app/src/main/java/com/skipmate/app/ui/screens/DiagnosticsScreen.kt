package com.skipmate.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skipmate.app.R
import com.skipmate.app.data.diagnostics.DiagnosticEvent
import com.skipmate.app.data.diagnostics.DiagnosticStatus
import com.skipmate.app.ui.components.AppTopBar
import com.skipmate.app.ui.theme.Primary
import com.skipmate.app.ui.theme.PrimaryContainer
import com.skipmate.app.ui.theme.StatusError
import com.skipmate.app.ui.theme.StatusNeutral
import com.skipmate.app.ui.theme.StatusSuccess
import com.skipmate.app.ui.theme.StatusWarning

@Composable
fun DiagnosticsScreen(
    events: List<DiagnosticEvent>,
    isAccessibilityEnabled: Boolean,
    isYouTubeInstalled: Boolean,
    isLoggingEnabled: Boolean,
    onClearLogs: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.diagnostics_title),
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Environment Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Diagnostic State Summary",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        DiagRow(
                            label = "Accessibility Service",
                            value = if (isAccessibilityEnabled) "Active" else "Disabled",
                            color = if (isAccessibilityEnabled) StatusSuccess else StatusWarning
                        )
                        DiagRow(
                            label = "YouTube Target Package",
                            value = if (isYouTubeInstalled) "Installed" else "Not Detected",
                            color = if (isYouTubeInstalled) StatusSuccess else StatusError
                        )
                        DiagRow(
                            label = "Local Diagnostic Logging",
                            value = if (isLoggingEnabled) "Enabled" else "Muted",
                            color = if (isLoggingEnabled) Primary else StatusNeutral
                        )
                        DiagRow(
                            label = "Recorded Event Entries",
                            value = events.size.toString(),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Action Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Diagnostic Log",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedButton(
                        onClick = onClearLogs,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Clear Logs", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (events.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = StatusNeutral,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(id = R.string.diagnostics_no_events),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(events, key = { it.id }) { event ->
                    DiagnosticEventCard(event = event)
                }
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Composable
private fun DiagnosticEventCard(event: DiagnosticEvent) {
    val statusColor = when (event.status) {
        DiagnosticStatus.CLICK_DISPATCH_SUCCESS -> StatusSuccess
        DiagnosticStatus.CLICK_ATTEMPTED -> Primary
        DiagnosticStatus.CANDIDATE_NOT_ACTIONABLE -> StatusWarning
        DiagnosticStatus.CLICK_DISPATCH_FAILED -> StatusError
        DiagnosticStatus.NO_CANDIDATE_FOUND -> StatusNeutral
        DiagnosticStatus.TARGET_APP_DETECTED -> Primary
        DiagnosticStatus.NO_RELEVANT_EVENT -> StatusNeutral
        DiagnosticStatus.PLAYBACK_TRANSITION_OBSERVED -> StatusSuccess
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.status.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
                Text(
                    text = event.formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!event.details.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.details,
                    style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
