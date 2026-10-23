package com.skipmate.app.data

import com.skipmate.app.data.auth.AuthResult
import com.skipmate.app.data.auth.AuthUser
import com.skipmate.app.data.billing.SubscriptionStatus
import com.skipmate.app.data.user.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun testUserProfileDefaults() {
        val profile = UserProfile(
            uid = "test_uid_123",
            name = "Abhishek K R",
            email = "abhishek@example.com"
        )

        assertEquals("test_uid_123", profile.uid)
        assertEquals("Abhishek K R", profile.name)
        assertEquals("abhishek@example.com", profile.email)
        assertEquals("free", profile.subscriptionStatus)
        assertEquals("free", profile.subscriptionPlan)
        assertTrue(profile.autoPayEnabled)
        assertFalse(profile.isProActive())
    }

    @Test
    fun testUserProfileProActive() {
        val proProfile = UserProfile(
            uid = "test_pro_123",
            name = "Pro User",
            email = "pro@example.com",
            subscriptionStatus = "active",
            subscriptionPlan = "yearly",
            autoPayEnabled = true
        )

        assertTrue(proProfile.isProActive())
        assertEquals(SubscriptionStatus.ACTIVE, SubscriptionStatus.fromString(proProfile.subscriptionStatus))
    }

    @Test
    fun testSubscriptionStatusParsing() {
        assertEquals(SubscriptionStatus.FREE, SubscriptionStatus.fromString("free"))
        assertEquals(SubscriptionStatus.PENDING, SubscriptionStatus.fromString("pending"))
        assertEquals(SubscriptionStatus.ACTIVE, SubscriptionStatus.fromString("active"))
        assertEquals(SubscriptionStatus.EXPIRED, SubscriptionStatus.fromString("expired"))
        assertEquals(SubscriptionStatus.CANCELLED, SubscriptionStatus.fromString("cancelled"))
        assertEquals(SubscriptionStatus.FREE, SubscriptionStatus.fromString("unknown_status"))
    }

    @Test
    fun testAuthResultTypes() {
        val user = AuthUser("uid_1", "test@example.com", "Test User", true)
        val success = AuthResult.Success(user)
        val error = AuthResult.Error("Invalid credentials")
        val loading = AuthResult.Loading

        assertEquals("uid_1", success.data.uid)
        assertEquals("Invalid credentials", error.message)
        assertNotNull(loading)
    }
}
