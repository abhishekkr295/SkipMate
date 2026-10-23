package com.skipmate.app.ui.screens

import android.content.Context
import com.skipmate.app.data.user.UserProfileRepository
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skipmate.app.R
import com.skipmate.app.ui.theme.Primary
import com.skipmate.app.ui.theme.StatusSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

enum class SubscriptionPlan {
    MONTHLY,
    YEARLY
}

enum class PaymentCategory {
    UPI,
    CARDS,
    NET_BANKING,
    WALLETS
}

data class CurrencyPricing(
    val countryCode: String,
    val currencyCode: String,
    val symbol: String,
    val monthlyPrice: String,
    val yearlyPrice: String,
    val regularYearlyPrice: String,
    val perMonthYearlyEquiv: String
)

val SupportedCurrencies = listOf(
    CurrencyPricing("IN", "INR", "₹", "9", "81", "108", "6.75"),
    CurrencyPricing("US", "USD", "$", "0.11", "0.99", "1.32", "0.08"),
    CurrencyPricing("EU", "EUR", "€", "0.10", "0.90", "1.20", "0.08"),
    CurrencyPricing("GB", "GBP", "£", "0.09", "0.81", "1.08", "0.07"),
    CurrencyPricing("JP", "JPY", "¥", "16", "144", "192", "12"),
    CurrencyPricing("CA", "CAD", "CA$", "0.15", "1.35", "1.80", "0.11"),
    CurrencyPricing("AU", "AUD", "AU$", "0.17", "1.53", "2.04", "0.13")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    userProfileRepository: UserProfileRepository? = null,
    currentUid: String = "",
    initialTrialMode: Boolean = false,
    onBack: () -> Unit
) {
    // Detect default country currency from user locale
    val defaultCountry = remember {
        val deviceCountry = Locale.getDefault().country.uppercase()
        SupportedCurrencies.find { it.countryCode == deviceCountry } ?: SupportedCurrencies.first()
    }

    var selectedCurrency by remember { mutableStateOf(defaultCountry) }
    var selectedPlan by remember { mutableStateOf(SubscriptionPlan.YEARLY) }
    var showHelpSheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isTrialMode by remember { mutableStateOf(initialTrialMode) }

    LaunchedEffect(initialTrialMode) {
        if (initialTrialMode) {
            isTrialMode = true
            showPaymentSheet = true
        }
    }
    var isAutoPayEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val offerPrefs = remember {
        context.getSharedPreferences("skipmate_offer_prefs", Context.MODE_PRIVATE)
    }

    // Persist offer end time so leaving and returning does NOT reset the timer!
    val offerEndTime = remember {
        val stored = offerPrefs.getLong("welcome_offer_end_time", 0L)
        if (stored == 0L) {
            val created = System.currentTimeMillis() + 3600 * 1000L
            offerPrefs.edit().putLong("welcome_offer_end_time", created).apply()
            created
        } else {
            stored
        }
    }

    var secondsRemaining by remember {
        val rem = maxOf(0L, (offerEndTime - System.currentTimeMillis()) / 1000L).toInt()
        mutableIntStateOf(rem)
    }

    LaunchedEffect(offerEndTime) {
        while (secondsRemaining > 0) {
            delay(1000)
            val rem = maxOf(0L, (offerEndTime - System.currentTimeMillis()) / 1000L).toInt()
            secondsRemaining = rem
        }
    }

    val minutes = (secondsRemaining / 60)
    val seconds = (secondsRemaining % 60)
    val timerString = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.subscription_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showHelpSheet = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.need_help),
                            color = Primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
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
            // Limited-Time 1-Hour Offer Banner (Workable: Clickable to activate Welcome Offer)
            if (secondsRemaining > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedPlan = SubscriptionPlan.YEARLY
                            isTrialMode = false
                            showPaymentSheet = true
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1435)
                    ),
                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)))),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEC4899).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFF472B6),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = R.string.limited_offer_title),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFDF2F8)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(id = R.string.limited_offer_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE9D5FF)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEC4899))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = timerString,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Yearly Plan: ${selectedCurrency.symbol}${selectedCurrency.yearlyPrice} / yr",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF472B6)
                            )
                            Text(
                                text = "Tap to claim 25% OFF offer →",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 7 Days Free Trial Hero Card (Workable: Clickable to activate 7-Day Free Trial)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isTrialMode = true
                        showPaymentSheet = true
                    },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.5.dp, if (isTrialMode) Primary else Primary.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.trial_badge),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFB45309)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Primary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.pro_badge),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(id = R.string.trial_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enjoy all premium features for 7 days with ${selectedCurrency.symbol}0 charge today. Cancel anytime before trial ends.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tap to start 7-day free trial →",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Primary
                        )
                    }
                }
            }

            // Currency Selector Row
            Column {
                Text(
                    text = stringResource(id = R.string.currency_selector),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SupportedCurrencies.forEach { currency ->
                        val isSelected = selectedCurrency.currencyCode == currency.currencyCode
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCurrency = currency },
                            label = {
                                Text(
                                    text = "${currency.symbol} ${currency.currencyCode}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Plan Selector Cards
            Text(
                text = "Choose Your Plan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Yearly Plan Card (25% Offer Highlight)
            PlanSelectionCard(
                planName = stringResource(id = R.string.plan_yearly),
                priceText = "${selectedCurrency.symbol}${selectedCurrency.yearlyPrice} / year",
                regularPriceText = "${selectedCurrency.symbol}${selectedCurrency.regularYearlyPrice}",
                perMonthText = "≈ ${selectedCurrency.symbol}${selectedCurrency.perMonthYearlyEquiv} / mo",
                badgeText = stringResource(id = R.string.plan_yearly_save),
                badgeColor = StatusSuccess,
                description = stringResource(id = R.string.plan_yearly_desc),
                isSelected = selectedPlan == SubscriptionPlan.YEARLY,
                onClick = { selectedPlan = SubscriptionPlan.YEARLY }
            )

            // Monthly Plan Card
            PlanSelectionCard(
                planName = stringResource(id = R.string.plan_monthly),
                priceText = "${selectedCurrency.symbol}${selectedCurrency.monthlyPrice} / month",
                regularPriceText = null,
                perMonthText = null,
                badgeText = "FLEXIBLE",
                badgeColor = Color(0xFF6366F1),
                description = stringResource(id = R.string.plan_monthly_desc),
                isSelected = selectedPlan == SubscriptionPlan.MONTHLY,
                onClick = { selectedPlan = SubscriptionPlan.MONTHLY }
            )

            // Prominent Move Next / Continue Button right under plan cards
            val planCtaLabel = if (selectedPlan == SubscriptionPlan.YEARLY) {
                "Continue with Yearly Plan (${selectedCurrency.symbol}${selectedCurrency.yearlyPrice} / yr) →"
            } else {
                "Continue with Monthly Plan (${selectedCurrency.symbol}${selectedCurrency.monthlyPrice} / mo) →"
            }

            Button(
                onClick = {
                    isTrialMode = false
                    showPaymentSheet = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = planCtaLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Included Pro Features
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(id = R.string.pro_features_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    FeatureRow(text = stringResource(id = R.string.sub_feature_1))
                    FeatureRow(text = stringResource(id = R.string.sub_feature_2))
                    FeatureRow(text = stringResource(id = R.string.sub_feature_3))
                    FeatureRow(text = stringResource(id = R.string.sub_feature_4))
                    FeatureRow(text = stringResource(id = R.string.sub_feature_5))
                }
            }

            // Main CTA Trial Button
            Button(
                onClick = {
                    isTrialMode = true
                    showPaymentSheet = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${stringResource(id = R.string.btn_start_trial)} (Due Today: 0 ${selectedCurrency.currencyCode})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Security & Privacy Note
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = StatusSuccess,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(id = R.string.payment_secure),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Payment Gateway Bottom Sheet
    if (showPaymentSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val coroutineScope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            PaymentGatewaySheetContent(
                selectedCurrency = selectedCurrency,
                selectedPlan = selectedPlan,
                isTrialMode = isTrialMode,
                isAutoPayEnabled = isAutoPayEnabled,
                onProceedPayment = {
                    coroutineScope.launch {
                        val status = if (isTrialMode) "trial" else "active"
                        val planStr = if (selectedPlan == SubscriptionPlan.YEARLY) "yearly" else "monthly"
                        userProfileRepository?.activateSubscription(
                            uid = currentUid,
                            status = status,
                            plan = planStr,
                            autoPayEnabled = true
                        )
                        sheetState.hide()
                        showPaymentSheet = false
                        showSuccessDialog = true
                    }
                },
                onCancel = {
                    coroutineScope.launch {
                        sheetState.hide()
                        showPaymentSheet = false
                    }
                }
            )
        }
    }

    // Need Help Bottom Sheet
    if (showHelpSheet) {
        val helpSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val coroutineScope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = helpSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            NeedHelpSheetContent(
                onClose = {
                    coroutineScope.launch {
                        helpSheetState.hide()
                        showHelpSheet = false
                    }
                }
            )
        }
    }

    // Subscription Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = StatusSuccess,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (isTrialMode) "7-Day Free Trial Activated!" else "SkipMate Pro Activated!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = if (isTrialMode) {
                        "Welcome to SkipMate Pro! Your 7-day free trial is now active. All video skip enhancements are enabled."
                    } else {
                        "Welcome to SkipMate Pro! Your ${if (selectedPlan == SubscriptionPlan.YEARLY) "Yearly" else "Monthly"} Plan has been activated successfully with Auto-Pay enabled. All video skip enhancements are unlocked."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(text = "Awesome!")
                }
            }
        )
    }
}

@Composable
private fun PlanSelectionCard(
    planName: String,
    priceText: String,
    regularPriceText: String?,
    perMonthText: String?,
    badgeText: String,
    badgeColor: Color,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Primary else MaterialTheme.colorScheme.outlineVariant,
        label = "borderColor"
    )
    val cardBackground by animateColorAsState(
        targetValue = if (isSelected) Primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        label = "cardBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isSelected,
                        onClick = onClick
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = planName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (regularPriceText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = regularPriceText,
                            style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (perMonthText != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = perMonthText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isSelected) "Selected ✓" else "Select Plan",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(StatusSuccess.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = StatusSuccess,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PaymentGatewaySheetContent(
    selectedCurrency: CurrencyPricing,
    selectedPlan: SubscriptionPlan,
    isTrialMode: Boolean = false,
    isAutoPayEnabled: Boolean = true,
    onProceedPayment: () -> Unit,
    onCancel: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(PaymentCategory.UPI) }
    var upiIdInput by remember { mutableStateOf("") }
    var cardNumberInput by remember { mutableStateOf("") }
    var cardExpiryInput by remember { mutableStateOf("") }
    var cardCvvInput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val amount = if (selectedPlan == SubscriptionPlan.YEARLY) {
        "${selectedCurrency.symbol}${selectedCurrency.yearlyPrice}"
    } else {
        "${selectedCurrency.symbol}${selectedCurrency.monthlyPrice}"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isTrialMode) "Start 7-Day Free Trial" else "SkipMate Pro Checkout",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isTrialMode) {
                        "Total Due Today: 0 ${selectedCurrency.currencyCode} (${selectedCurrency.symbol}0)"
                    } else {
                        "Total Payable: $amount / ${if (selectedPlan == SubscriptionPlan.YEARLY) "year" else "month"}"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = StatusSuccess
                )
                if (isTrialMode) {
                    Text(
                        text = "Then $amount / ${if (selectedPlan == SubscriptionPlan.YEARLY) "year" else "month"} automatically billed after 7 days",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (isAutoPayEnabled) {
                        "🔄 Recurring Auto-Pay: Automatically set to renew every ${if (selectedPlan == SubscriptionPlan.YEARLY) "year" else "month"}."
                    } else {
                        "🔄 Recurring Auto-Pay: Inactive"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Payment Method Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentTabChip("UPI", Icons.Default.QrCode, selectedCategory == PaymentCategory.UPI) {
                selectedCategory = PaymentCategory.UPI
            }
            PaymentTabChip("Cards", Icons.Default.CreditCard, selectedCategory == PaymentCategory.CARDS) {
                selectedCategory = PaymentCategory.CARDS
            }
            PaymentTabChip("NetBanking", Icons.Default.AccountBalance, selectedCategory == PaymentCategory.NET_BANKING) {
                selectedCategory = PaymentCategory.NET_BANKING
            }
            PaymentTabChip("Wallets", Icons.Default.AccountBalanceWallet, selectedCategory == PaymentCategory.WALLETS) {
                selectedCategory = PaymentCategory.WALLETS
            }
        }

        // Method-specific input UI
        when (selectedCategory) {
            PaymentCategory.UPI -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Popular UPI Apps",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        UpiAppBadge("Google Pay")
                        UpiAppBadge("PhonePe")
                        UpiAppBadge("Paytm")
                        UpiAppBadge("BHIM")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = upiIdInput,
                        onValueChange = { upiIdInput = it },
                        label = { Text("Enter UPI ID (e.g. yourname@okhdfc)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            PaymentCategory.CARDS -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cardNumberInput,
                        onValueChange = { cardNumberInput = it },
                        label = { Text("Card Number (Visa, Mastercard, RuPay)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cardExpiryInput,
                            onValueChange = { cardExpiryInput = it },
                            label = { Text("MM/YY") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = cardCvvInput,
                            onValueChange = { cardCvvInput = it },
                            label = { Text("CVV") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }

            PaymentCategory.NET_BANKING -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NetBanking — Coming Soon",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Direct bank gateway integration is currently in progress. Please choose UPI or Cards for instant activation.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            PaymentCategory.WALLETS -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Wallets — Coming Soon",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Digital Wallets & Google Play billing integration are currently in progress. Please choose UPI or Cards for instant activation.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Action Buttons
        val isComingSoon = selectedCategory == PaymentCategory.NET_BANKING || selectedCategory == PaymentCategory.WALLETS
        val payButtonText = when {
            isComingSoon -> "Coming Soon — Select UPI or Cards"
            isTrialMode -> "${stringResource(id = R.string.pay_now)} 0 ${selectedCurrency.currencyCode}"
            else -> "${stringResource(id = R.string.pay_now)} $amount"
        }

        Button(
            onClick = {
                if (!isComingSoon) {
                    isProcessing = true
                    onProceedPayment()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isComingSoon) MaterialTheme.colorScheme.surfaceVariant else Primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            enabled = !isProcessing && !isComingSoon
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = payButtonText,
                    fontWeight = FontWeight.Bold,
                    color = if (isComingSoon) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                )
            }
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.cancel))
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun PaymentTabChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun UpiAppBadge(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun NeedHelpSheetContent(
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Frequently Asked Questions",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        FaqItem(
            question = "How does the 7-day free trial work?",
            answer = "You get full access to all SkipMate Pro features for 7 full days completely free. If you don't cancel before the 7 days end, your subscription continues at the plan rate."
        )

        FaqItem(
            question = "Can I cancel my subscription anytime?",
            answer = "Yes! You can cancel anytime with a single click in Android Settings > Subscriptions or through SkipMate settings. You retain access until the end of your billing cycle."
        )

        FaqItem(
            question = "How does multi-currency pricing work?",
            answer = "We automatically convert the base ₹9/mo price into your local currency (USD, EUR, GBP, CAD, AUD, JPY) so there are no surprise foreign exchange fees."
        )

        FaqItem(
            question = "What payment methods are supported?",
            answer = "We support UPI (GPay, PhonePe, Paytm), all major Credit & Debit cards (Visa, Mastercard, RuPay), Net Banking with over 50 banks, and Google Play In-App Billing."
        )

        FaqItem(
            question = "Need developer assistance?",
            answer = "Contact founder Abhishek K R directly at support@skipmate.local. We respond within 24 hours."
        )

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Close")
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = answer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
