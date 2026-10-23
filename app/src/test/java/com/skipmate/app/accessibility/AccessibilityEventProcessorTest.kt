package com.skipmate.app.accessibility

import com.skipmate.app.data.diagnostics.DiagnosticRepository
import com.skipmate.app.data.diagnostics.DiagnosticStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccessibilityEventProcessorTest {

    private lateinit var processor: AccessibilityEventProcessor
    private lateinit var diagRepo: DiagnosticRepository

    @Before
    fun setUp() {
        diagRepo = DiagnosticRepository()
        processor = AccessibilityEventProcessor(
            matcher = SkipControlMatcher(),
            diagnosticRepository = diagRepo,
            debounceWindowMs = 500L
        )
    }

    // Scenario 9: Service disabled
    @Test
    fun testServiceDisabledIgnoresEvents() {
        processor.isServiceEnabled = false
        val node = TestAccessibilityNode(text = "Skip Ad")

        val result = processor.processEvent(
            packageName = "com.google.android.youtube",
            eventType = 32, // TYPE_WINDOW_STATE_CHANGED
            rootNode = node
        )

        assertFalse(result.candidateFound)
        assertFalse(result.clickAttempted)
        assertEquals(0, node.clickActionCount)
        assertEquals(DiagnosticStatus.NO_RELEVANT_EVENT, diagRepo.latestEvent.value?.status)
    }

    // Scenario 10: Target application mismatch
    @Test
    fun testTargetApplicationMismatchIgnored() {
        processor.targetPackageName = "com.google.android.youtube"
        val node = TestAccessibilityNode(text = "Skip Ad")

        val result = processor.processEvent(
            packageName = "com.other.app",
            eventType = 32,
            rootNode = node
        )

        assertFalse(result.candidateFound)
        assertFalse(result.clickAttempted)
        assertEquals(0, node.clickActionCount)
    }

    // Scenario 8: Duplicate accessibility event and debounce throttling
    @Test
    fun testDebouncePreventsRapidDuplicateClicks() {
        val node = TestAccessibilityNode(text = "Skip Ad")

        // First event succeeds
        val result1 = processor.processEvent(
            packageName = "com.google.android.youtube",
            eventType = 32,
            rootNode = node
        )
        assertTrue("First click must succeed", result1.clickAttempted)
        assertTrue(result1.frameworkAcceptedClick)
        assertEquals(1, node.clickActionCount)

        // Immediate duplicate event within debounce window is throttled
        val result2 = processor.processEvent(
            packageName = "com.google.android.youtube",
            eventType = 2048, // TYPE_WINDOW_CONTENT_CHANGED
            rootNode = node
        )
        assertFalse("Duplicate rapid event must be throttled", result2.clickAttempted)
        assertEquals(1, node.clickActionCount)
        assertEquals(DiagnosticStatus.CANDIDATE_NOT_ACTIONABLE, diagRepo.latestEvent.value?.status)
    }

    // Scenario 12: Stale or unavailable node
    @Test
    fun testStaleNodeHandling() {
        val staleNode = TestAccessibilityNode(
            text = "Skip Ad",
            shouldThrowOnAction = true
        )

        val result = processor.processEvent(
            packageName = "com.google.android.youtube",
            eventType = 32,
            rootNode = staleNode
        )

        assertTrue(result.candidateFound)
        assertFalse("Framework click should fail when node throws exception", result.frameworkAcceptedClick)
        assertEquals(DiagnosticStatus.CLICK_DISPATCH_FAILED, diagRepo.latestEvent.value?.status)
    }

    // Successful action dispatch test
    @Test
    fun testSuccessfulClickActionDispatch() {
        val node = TestAccessibilityNode(
            text = "Skip Ads",
            viewIdResourceName = "com.google.android.youtube:id/skip_ad_button"
        )

        val result = processor.processEvent(
            packageName = "com.google.android.youtube",
            eventType = 32,
            rootNode = node
        )

        assertTrue(result.candidateFound)
        assertTrue(result.isActionable)
        assertTrue(result.clickAttempted)
        assertTrue(result.frameworkAcceptedClick)
        assertEquals(1, node.clickActionCount)
        assertEquals(DiagnosticStatus.CLICK_DISPATCH_SUCCESS, diagRepo.latestEvent.value?.status)
    }
}
