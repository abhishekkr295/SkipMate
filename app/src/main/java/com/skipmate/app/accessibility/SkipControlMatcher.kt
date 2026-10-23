package com.skipmate.app.accessibility

import java.util.Locale

class SkipControlMatcher {

    companion object {
        // Known YouTube resource IDs for skip buttons across multiple app releases
        val KNOWN_RESOURCE_IDS = setOf(
            "com.google.android.youtube:id/skip_ad_button",
            "com.google.android.youtube:id/modern_skip_ad_button",
            "com.google.android.youtube:id/ad_skip_button",
            "com.google.android.youtube:id/skip_ad_button_text",
            "com.google.android.youtube:id/sub_skip_ad_button"
        )

        // Localized exact and core phrases for Skip Ad buttons
        val RECOGNIZED_SKIP_PHRASES = setOf(
            "skip ad",
            "skip ads",
            "skip",
            "omitir anuncio",
            "omitir anuncios",
            "omitir",
            "passer l'annonce",
            "passer les annonces",
            "passer",
            "werbung überspringen",
            "überspringen",
            "pular anúncio",
            "pular anúncios",
            "pular",
            "salta annuncio",
            "salta",
            "пропустить",
            "пропустить объявление",
            "広告をスキップ",
            "スキップ",
            "광고 건너뛰기",
            "건너뛰기",
            "跳过广告",
            "略過廣告",
            "跳過",
            "विज्ञापन छोड़ें",
            "छोड़ें",
            "lewati iklan",
            "lewati",
            "reklamı atla",
            "atla",
            "تخطي الإعلان",
            "تخطي"
        )

        // Negative filters to prevent false-positive clicks on video navigation controls
        val EXCLUSION_PATTERNS = listOf(
            "skip forward",
            "skip backward",
            "skip to next",
            "skip chapter",
            "skip 10",
            "skip 5",
            "skip intro",
            "skip trial",
            "skip sign in",
            "skip survey",
            "skip countdown"
        )
    }

    data class MatchCandidate(
        val nodeToClick: AccessibilityNodeWrapper,
        val matchedRule: String,
        val matchedText: String?,
        val resourceName: String?
    )

    fun findSkipControl(root: AccessibilityNodeWrapper?): MatchCandidate? {
        if (root == null) return null
        return searchTree(root, 0, mutableSetOf())
    }

    private fun searchTree(
        node: AccessibilityNodeWrapper,
        depth: Int,
        visited: MutableSet<Int>
    ): MatchCandidate? {
        if (depth > 40) return null

        val nodeIdentity = System.identityHashCode(node)
        if (!visited.add(nodeIdentity)) return null

        // Check current node
        val match = evaluateNode(node)
        if (match != null) {
            return match
        }

        // Search children
        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            val childMatch = searchTree(child, depth + 1, visited)
            if (childMatch != null) {
                return childMatch
            }
        }

        return null
    }

    fun evaluateNode(node: AccessibilityNodeWrapper): MatchCandidate? {
        val resourceId = node.viewIdResourceName?.lowercase(Locale.ROOT)
        val text = node.text?.toString()?.trim()
        val contentDesc = node.contentDescription?.toString()?.trim()

        val textLower = text?.lowercase(Locale.ROOT)
        val descLower = contentDesc?.lowercase(Locale.ROOT)

        // Check exclusion patterns first
        if (matchesExclusion(textLower) || matchesExclusion(descLower)) {
            return null
        }

        // Tier 1: Resource ID Match
        if (resourceId != null && KNOWN_RESOURCE_IDS.any { resourceId.contains(it) || it.contains(resourceId) }) {
            val clickableTarget = resolveClickableTarget(node)
            return MatchCandidate(
                nodeToClick = clickableTarget ?: node,
                matchedRule = "RESOURCE_ID_MATCH",
                matchedText = text ?: contentDesc ?: resourceId,
                resourceName = resourceId
            )
        }

        // Tier 2: Text Match
        if (!textLower.isNullOrEmpty() && isRecognizedSkipPhrase(textLower)) {
            val clickableTarget = resolveClickableTarget(node)
            return MatchCandidate(
                nodeToClick = clickableTarget ?: node,
                matchedRule = "LOCALIZED_TEXT_MATCH",
                matchedText = text,
                resourceName = resourceId
            )
        }

        // Tier 3: Content Description Match
        if (!descLower.isNullOrEmpty() && isRecognizedSkipPhrase(descLower)) {
            val clickableTarget = resolveClickableTarget(node)
            return MatchCandidate(
                nodeToClick = clickableTarget ?: node,
                matchedRule = "CONTENT_DESCRIPTION_MATCH",
                matchedText = contentDesc,
                resourceName = resourceId
            )
        }

        return null
    }

    private fun isRecognizedSkipPhrase(input: String): Boolean {
        // Direct match
        if (RECOGNIZED_SKIP_PHRASES.contains(input)) return true

        // Clean punctuation and check
        val cleaned = input.replace(Regex("[.,:!؟]"), "").trim()
        if (RECOGNIZED_SKIP_PHRASES.contains(cleaned)) return true

        // Check if string starts with "skip ad" or ends with localized word
        if (cleaned.startsWith("skip ad") || cleaned.startsWith("skip in")) {
            // "Skip in 5s" is a countdown, not a clickable button yet!
            if (cleaned.contains("in ") && cleaned.matches(Regex(".*\\d+.*"))) {
                return false
            }
            return true
        }

        return false
    }

    private fun matchesExclusion(input: String?): Boolean {
        if (input == null) return false
        for (pattern in EXCLUSION_PATTERNS) {
            if (input.contains(pattern)) return true
        }
        return false
    }

    fun resolveClickableTarget(node: AccessibilityNodeWrapper): AccessibilityNodeWrapper? {
        if (node.isClickable) return node

        // Check up to 3 ancestors for a clickable container (FrameLayout/LinearLayout)
        var current: AccessibilityNodeWrapper? = node.getParent()
        var depth = 0
        while (current != null && depth < 3) {
            if (current.isClickable) {
                return current
            }
            current = current.getParent()
            depth++
        }
        return null
    }

    fun isNodeActionable(node: AccessibilityNodeWrapper): Boolean {
        return node.isVisibleToUser && node.isEnabled && node.isClickable
    }
}
