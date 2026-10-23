package com.skipmate.app.data.billing

enum class SubscriptionStatus(val value: String) {
    FREE("free"),
    PENDING("pending"),
    ACTIVE("active"),
    EXPIRED("expired"),
    CANCELLED("cancelled");

    companion object {
        fun fromString(status: String?): SubscriptionStatus {
            return entries.find { it.value.equals(status, ignoreCase = true) } ?: FREE
        }
    }
}

enum class SubscriptionTier(val planId: String, val title: String) {
    FREE("free", "SkipMate Basic"),
    MONTHLY("monthly", "SkipMate Pro Monthly"),
    YEARLY("yearly", "SkipMate Pro Yearly")
}
