package com.skipmate.app.accessibility

import com.skipmate.app.data.diagnostics.DiagnosticEvent
import com.skipmate.app.data.diagnostics.DiagnosticRepository
import com.skipmate.app.data.diagnostics.DiagnosticStatus

class AccessibilityEventProcessor(
    private val matcher: SkipControlMatcher = SkipControlMatcher(),
    private val diagnosticRepository: DiagnosticRepository? = null,
    private val debounceWindowMs: Long = 1000L
) {
    private var lastClickTimestamp: Long = 0L
    private var lastClickedRule: String? = null

    var isServiceEnabled: Boolean = true
    var targetPackageName: String = "com.google.android.youtube"
    var isDiagnosticLoggingEnabled: Boolean = true

    fun processEvent(
        packageName: CharSequence?,
        eventType: Int,
        rootNode: AccessibilityNodeWrapper?
    ): ClickAttemptResult {
        // Guard 1: Service enabled check
        if (!isServiceEnabled) {
            logDiagnostic(
                DiagnosticStatus.NO_RELEVANT_EVENT,
                "Event ignored: SkipMate service is disabled in settings."
            )
            return ClickAttemptResult.noCandidate()
        }

        // Guard 2: Target application check
        val eventPkg = packageName?.toString()
        if (eventPkg == null || !eventPkg.equals(targetPackageName, ignoreCase = true)) {
            // Ignored silently or with minimal diagnostic to avoid spamming for background windows
            return ClickAttemptResult.noCandidate()
        }

        logDiagnostic(
            DiagnosticStatus.TARGET_APP_DETECTED,
            "Target application active: $eventPkg"
        )

        // Guard 3: Root node availability check
        if (rootNode == null) {
            logDiagnostic(
                DiagnosticStatus.NO_CANDIDATE_FOUND,
                "Accessibility tree root node is null or detached."
            )
            return ClickAttemptResult.noCandidate()
        }

        // Search for Skip Ads control
        val candidate = try {
            matcher.findSkipControl(rootNode)
        } catch (e: Exception) {
            logDiagnostic(
                DiagnosticStatus.NO_CANDIDATE_FOUND,
                "Error during UI tree traversal: ${e.message}"
            )
            return ClickAttemptResult.noCandidate()
        }

        if (candidate == null) {
            logDiagnostic(
                DiagnosticStatus.NO_CANDIDATE_FOUND,
                "No candidate skip control identified in current hierarchy."
            )
            return ClickAttemptResult.noCandidate()
        }

        // Guard 4: Validate node actionability
        val targetNode = candidate.nodeToClick
        if (!matcher.isNodeActionable(targetNode)) {
            val reason = "Candidate found (${candidate.matchedRule}) but not actionable: visible=${targetNode.isVisibleToUser}, enabled=${targetNode.isEnabled}, clickable=${targetNode.isClickable}"
            logDiagnostic(
                DiagnosticStatus.CANDIDATE_NOT_ACTIONABLE,
                reason,
                "Matched text: ${candidate.matchedText}, Resource: ${candidate.resourceName}"
            )
            return ClickAttemptResult.notActionable(candidate.matchedRule, candidate.matchedText, reason)
        }

        // Guard 5: Debounce and duplicate-event protection
        val now = System.currentTimeMillis()
        if (now - lastClickTimestamp < debounceWindowMs) {
            logDiagnostic(
                DiagnosticStatus.CANDIDATE_NOT_ACTIONABLE,
                "Click throttled by debounce safety window (${now - lastClickTimestamp}ms < ${debounceWindowMs}ms)."
            )
            return ClickAttemptResult.notActionable(
                candidate.matchedRule,
                candidate.matchedText,
                "Throttled by debounce"
            )
        }

        // Dispatch click action
        logDiagnostic(
            DiagnosticStatus.CLICK_ATTEMPTED,
            "Attempting ACTION_CLICK via rule [${candidate.matchedRule}]",
            "Target: ${candidate.matchedText ?: candidate.resourceName}"
        )

        val success = try {
            targetNode.performAction(AccessibilityNodeWrapper.ACTION_CLICK)
        } catch (e: Exception) {
            logDiagnostic(
                DiagnosticStatus.CLICK_DISPATCH_FAILED,
                "Exception dispatching ACTION_CLICK: ${e.message}"
            )
            return ClickAttemptResult.executed(
                accepted = false,
                matchedRule = candidate.matchedRule,
                matchedText = candidate.matchedText,
                resourceName = candidate.resourceName
            )
        }

        lastClickTimestamp = now
        lastClickedRule = candidate.matchedRule

        if (success) {
            logDiagnostic(
                DiagnosticStatus.CLICK_DISPATCH_SUCCESS,
                "ACTION_CLICK accepted by Android accessibility framework.",
                "Note: Framework acceptance confirms click dispatch, not video skip confirmation."
            )
        } else {
            logDiagnostic(
                DiagnosticStatus.CLICK_DISPATCH_FAILED,
                "ACTION_CLICK rejected by Android accessibility framework or node is stale."
            )
        }

        return ClickAttemptResult.executed(
            accepted = success,
            matchedRule = candidate.matchedRule,
            matchedText = candidate.matchedText,
            resourceName = candidate.resourceName
        )
    }

    private var lastLoggedStatus: DiagnosticStatus? = null
    private var lastNoCandidateLogTime: Long = 0L

    private fun logDiagnostic(status: DiagnosticStatus, summary: String, details: String? = null) {
        if (!isDiagnosticLoggingEnabled) return
        val now = System.currentTimeMillis()
        if (status == DiagnosticStatus.NO_CANDIDATE_FOUND) {
            // Deduplicate routine background scans during normal video playback: at most once every 5 seconds
            if (lastLoggedStatus == DiagnosticStatus.NO_CANDIDATE_FOUND && (now - lastNoCandidateLogTime) < 5000L) {
                return
            }
            lastNoCandidateLogTime = now
        }
        lastLoggedStatus = status
        diagnosticRepository?.recordEvent(
            DiagnosticEvent(
                status = status,
                summary = summary,
                details = details
            )
        )
    }

    fun resetDebounce() {
        lastClickTimestamp = 0L
        lastClickedRule = null
    }
}
