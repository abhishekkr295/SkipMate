package com.skipmate.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skipmate.app.R
import com.skipmate.app.ui.components.AppTopBar
import com.skipmate.app.ui.theme.Primary

@Composable
fun AboutScreen(
    onRateOnPlayStore: () -> Unit = {},
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.about_title),
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_skipmate_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.about_app_name),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(id = R.string.app_tagline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(id = R.string.app_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                    }
                }
            }

            // About the App
            AboutDetailCard(
                icon = Icons.Default.Info,
                iconColor = Primary,
                title = stringResource(id = R.string.about_app_heading),
                body = stringResource(id = R.string.about_app_body)
            )

            // Our Mission
            AboutDetailCard(
                icon = Icons.Default.RocketLaunch,
                iconColor = Color(0xFF6366F1),
                title = stringResource(id = R.string.about_mission_heading),
                body = stringResource(id = R.string.about_mission_body)
            )

            // Founder & Developer
            AboutDetailCard(
                icon = Icons.Default.Person,
                iconColor = Color(0xFF38BDF8),
                title = stringResource(id = R.string.about_founder_heading),
                body = stringResource(id = R.string.about_founder_name),
                subtitle = stringResource(id = R.string.about_founder_role),
                footer = stringResource(id = R.string.about_founder_contact)
            )

            // Rate Us on Google Play Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRateOnPlayStore),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.rate_us_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(id = R.string.rate_us_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Google Play Accessibility Disclosure
            AboutDetailCard(
                icon = Icons.Default.Shield,
                iconColor = Color(0xFF0EA5E9),
                title = stringResource(id = R.string.about_accessibility_disclosure_title),
                body = stringResource(id = R.string.about_accessibility_disclosure_body),
                footer = stringResource(id = R.string.about_accessibility_footer)
            )

            // Copyright Information
            AboutDetailCard(
                icon = Icons.Default.Gavel,
                iconColor = Color(0xFF8B5CF6),
                title = stringResource(id = R.string.about_copyright_title),
                body = stringResource(id = R.string.about_copyright_body)
            )

            // Cookies & Storage Policy
            AboutDetailCard(
                icon = Icons.Default.Cookie,
                iconColor = Color(0xFFF59E0B),
                title = stringResource(id = R.string.about_cookies_title),
                body = stringResource(id = R.string.about_cookies_body)
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun AboutDetailCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    body: String,
    subtitle: String? = null,
    footer: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!footer.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = footer,
                        style = MaterialTheme.typography.labelSmall,
                        color = iconColor
                    )
                }
            }
        }
    }
}
