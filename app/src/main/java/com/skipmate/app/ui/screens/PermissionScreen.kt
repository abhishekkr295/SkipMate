package com.skipmate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skipmate.app.R
import com.skipmate.app.ui.components.AppTopBar
import com.skipmate.app.ui.components.PrimaryButton
import com.skipmate.app.ui.theme.Primary
import com.skipmate.app.ui.theme.PrimaryContainer
import com.skipmate.app.ui.theme.StatusSuccess

@Composable
fun PermissionScreen(
    isPermissionGranted: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.permission_guide_title),
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Status Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPermissionGranted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isPermissionGranted) StatusSuccess else Primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (isPermissionGranted) "Accessibility Service Active" else "Setup Required",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isPermissionGranted) Color(0xFF1E232A) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPermissionGranted)
                                "SkipMate is ready to detect skip buttons in YouTube."
                            else
                                "Follow the 4 steps below to enable automatic skipping.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isPermissionGranted) Color(0xFF4A5568) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Step-by-Step Setup",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1
            StepItem(
                number = "1",
                title = stringResource(id = R.string.permission_step_1),
                description = stringResource(id = R.string.permission_step_1_desc),
                icon = Icons.Default.Settings
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step 2
            StepItem(
                number = "2",
                title = stringResource(id = R.string.permission_step_2),
                description = stringResource(id = R.string.permission_step_2_desc),
                icon = Icons.Default.Visibility
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step 3
            StepItem(
                number = "3",
                title = stringResource(id = R.string.permission_step_3),
                description = stringResource(id = R.string.permission_step_3_desc),
                icon = Icons.Default.Lock
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step 4
            StepItem(
                number = "4",
                title = stringResource(id = R.string.permission_step_4),
                description = stringResource(id = R.string.permission_step_4_desc),
                icon = Icons.Default.CheckCircle
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Open Settings Button
            PrimaryButton(
                text = stringResource(id = R.string.permission_open_settings_button),
                onClick = onOpenAccessibilitySettings,
                icon = Icons.Default.Launch
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy & Disclosure Note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Why is this needed?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Android Accessibility Services are designed to inspect screen elements and perform clicks on your behalf. SkipMate only reads YouTube's interface to locate the official 'Skip Ads' button and never records video content or touches other apps.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    number: String,
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelLarge,
                    color = Primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
