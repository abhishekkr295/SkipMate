package com.skipmate.app.accessibility

data class ClickAttemptResult(
    val candidateFound: Boolean,
    val isActionable: Boolean,
    val clickAttempted: Boolean,
    val frameworkAcceptedClick: Boolean,
    val matchedRule: String? = null,
    val matchedText: String? = null,
    val targetResourceName: String? = null,
    val errorMessage: String? = null
) {
    companion object {
        fun noCandidate(): ClickAttemptResult = ClickAttemptResult(
            candidateFound = false,
            isActionable = false,
            clickAttempted = false,
            frameworkAcceptedClick = false
        )

        fun notActionable(matchedRule: String, matchedText: String?, reason: String): ClickAttemptResult = ClickAttemptResult(
            candidateFound = true,
            isActionable = false,
            clickAttempted = false,
            frameworkAcceptedClick = false,
            matchedRule = matchedRule,
            matchedText = matchedText,
            errorMessage = reason
        )

        fun executed(
            accepted: Boolean,
            matchedRule: String,
            matchedText: String?,
            resourceName: String?
        ): ClickAttemptResult = ClickAttemptResult(
            candidateFound = true,
            isActionable = true,
            clickAttempted = true,
            frameworkAcceptedClick = accepted,
            matchedRule = matchedRule,
            matchedText = matchedText,
            targetResourceName = resourceName
        )
    }
}
