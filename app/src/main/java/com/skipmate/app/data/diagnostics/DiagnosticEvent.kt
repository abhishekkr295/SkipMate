package com.skipmate.app.data.diagnostics

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class DiagnosticStatus {
    NO_RELEVANT_EVENT,
    TARGET_APP_DETECTED,
    NO_CANDIDATE_FOUND,
    CANDIDATE_NOT_ACTIONABLE,
    CLICK_ATTEMPTED,
    CLICK_DISPATCH_SUCCESS,
    CLICK_DISPATCH_FAILED,
    PLAYBACK_TRANSITION_OBSERVED
}

data class DiagnosticEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val status: DiagnosticStatus,
    val summary: String,
    val details: String? = null
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
