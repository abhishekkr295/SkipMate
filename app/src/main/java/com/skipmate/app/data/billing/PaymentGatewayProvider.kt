package com.skipmate.app.data.billing

import android.app.Activity

sealed class PaymentResult {
    data class Success(val transactionId: String, val provider: String) : PaymentResult()
    data class Error(val message: String, val code: Int = -1) : PaymentResult()
    object Cancelled : PaymentResult()
}

interface PaymentGatewayProvider {
    val providerName: String
    suspend fun initialize(): Result<Unit>
    suspend fun launchPurchaseFlow(
        activity: Activity,
        planId: String,
        currencyCode: String,
        amount: String,
        onResult: (PaymentResult) -> Unit
    )
    suspend fun restorePurchases(onResult: (PaymentResult) -> Unit)
}
