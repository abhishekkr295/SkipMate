package com.skipmate.app.data.user

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val subscriptionStatus: String = "free", // "free", "pending", "active", "expired", "cancelled"
    val subscriptionPlan: String = "free",   // "free", "monthly", "yearly"
    val autoPayEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isProActive(): Boolean = 
        subscriptionStatus.equals("active", ignoreCase = true) || 
        subscriptionStatus.equals("trial", ignoreCase = true)
}
