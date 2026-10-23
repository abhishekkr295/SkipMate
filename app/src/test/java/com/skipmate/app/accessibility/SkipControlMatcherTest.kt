package com.skipmate.app.accessibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SkipControlMatcherTest {

    private lateinit var matcher: SkipControlMatcher

    @Before
    fun setUp() {
        matcher = SkipControlMatcher()
    }

    // Scenario 1: Exact recognized Skip Ads label
    @Test
    fun testExactRecognizedSkipAdsLabel() {
        val node = TestAccessibilityNode(
            text = "Skip Ads",
            viewIdResourceName = "com.google.android.youtube:id/skip_ad_button"
        )
        val candidate = matcher.findSkipControl(node)
        assertNotNull("Candidate should be detected for 'Skip Ads'", candidate)
        assertEquals("RESOURCE_ID_MATCH", candidate?.matchedRule)
        assertTrue(matcher.isNodeActionable(candidate!!.nodeToClick))
    }

    // Scenario 2: Supported localized label (Spanish, French, German, Japanese, Hindi)
    @Test
    fun testSupportedLocalizedLabels() {
        val localizedPhrases = listOf(
            "Omitir anuncio",
            "Passer l'annonce",
            "Überspringen",
            "Pular anúncio",
            "広告をスキップ",
            "건너뛰기",
            "विज्ञापन छोड़ें"
        )

        for (phrase in localizedPhrases) {
            val node = TestAccessibilityNode(text = phrase)
            val candidate = matcher.findSkipControl(node)
            assertNotNull("Candidate should be detected for localized text: '$phrase'", candidate)
            assertEquals("LOCALIZED_TEXT_MATCH", candidate?.matchedRule)
            assertEquals(phrase, candidate?.matchedText)
        }
    }

    // Scenario 3: Unrelated button containing similar wording
    @Test
    fun testUnrelatedButtonWithSimilarWordingIsRejected() {
        val falsePositives = listOf(
            "Skip forward 10 seconds",
            "Skip backward",
            "Skip chapter",
            "Skip intro",
            "Skip trial",
            "Skip sign in",
            "Skip survey",
            "Skip in 5s"
        )

        for (fp in falsePositives) {
            val node = TestAccessibilityNode(text = fp)
            val candidate = matcher.findSkipControl(node)
            assertNull("Candidate with text '$fp' should NOT match", candidate)
        }
    }

    // Scenario 4: Non-clickable candidate (resolves clickable ancestor or flags not actionable)
    @Test
    fun testNonClickableCandidateAncestorResolution() {
        // Enclosing clickable button with non-clickable TextView child
        val parentContainer = TestAccessibilityNode(
            isClickable = true,
            className = "android.widget.FrameLayout"
        )
        val childTextView = TestAccessibilityNode(
            text = "Skip Ad",
            isClickable = false,
            className = "android.widget.TextView"
        )
        parentContainer.addChild(childTextView)

        val candidate = matcher.findSkipControl(parentContainer)
        assertNotNull(candidate)
        assertEquals(parentContainer, candidate?.nodeToClick)
        assertTrue(matcher.isNodeActionable(candidate!!.nodeToClick))

        // Fully non-clickable tree
        val nonClickableRoot = TestAccessibilityNode(
            text = "Skip Ad",
            isClickable = false
        )
        val nonClickableCandidate = matcher.findSkipControl(nonClickableRoot)
        assertNotNull(nonClickableCandidate)
        assertFalse(matcher.isNodeActionable(nonClickableCandidate!!.nodeToClick))
    }

    // Scenario 5: Disabled candidate
    @Test
    fun testDisabledCandidateIsNotActionable() {
        val node = TestAccessibilityNode(
            text = "Skip Ad",
            isEnabled = false
        )
        val candidate = matcher.findSkipControl(node)
        assertNotNull(candidate)
        assertFalse("Disabled node must not be actionable", matcher.isNodeActionable(candidate!!.nodeToClick))
    }

    // Scenario 6: Missing text and content description
    @Test
    fun testMissingTextAndContentDescription() {
        val node = TestAccessibilityNode(
            text = null,
            contentDescription = null,
            viewIdResourceName = null
        )
        val candidate = matcher.findSkipControl(node)
        assertNull("Empty node without identifiers should not match", candidate)
    }

    // Scenario 7: Null accessibility node
    @Test
    fun testNullAccessibilityNode() {
        val candidate = matcher.findSkipControl(null)
        assertNull("Null root node must return null without crashing", candidate)
    }

    // Scenario 11: No available skip control
    @Test
    fun testNoAvailableSkipControl() {
        val root = TestAccessibilityNode(text = "Subscribe to channel")
        root.addChild(TestAccessibilityNode(text = "Like"))
        root.addChild(TestAccessibilityNode(text = "Share"))
        root.addChild(TestAccessibilityNode(text = "Download"))

        val candidate = matcher.findSkipControl(root)
        assertNull("Unrelated video interface must produce no candidate", candidate)
    }

    // Invisible candidate check
    @Test
    fun testInvisibleCandidateIsNotActionable() {
        val node = TestAccessibilityNode(
            text = "Skip Ad",
            isVisibleToUser = false
        )
        val candidate = matcher.findSkipControl(node)
        assertNotNull(candidate)
        assertFalse("Invisible node must not be actionable", matcher.isNodeActionable(candidate!!.nodeToClick))
    }
}
