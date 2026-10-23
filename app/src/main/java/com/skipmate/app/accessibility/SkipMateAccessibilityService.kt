package com.skipmate.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.skipmate.app.SkipMateApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SkipMateAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var settingsJob: Job? = null

    private lateinit var eventProcessor: AccessibilityEventProcessor

    override fun onCreate() {
        super.onCreate()
        val app = applicationContext as? SkipMateApplication
        val diagnosticRepo = app?.diagnosticRepository
        eventProcessor = AccessibilityEventProcessor(
            matcher = SkipControlMatcher(),
            diagnosticRepository = diagnosticRepo
        )

        // Observe settings changes dynamically
        val settingsRepo = app?.settingsRepository
        if (settingsRepo != null) {
            settingsJob = serviceScope.launch {
                settingsRepo.userSettingsFlow.collectLatest { settings ->
                    eventProcessor.isServiceEnabled = settings.isAutoSkipEnabled
                    eventProcessor.targetPackageName = settings.targetPackageName
                    eventProcessor.isDiagnosticLoggingEnabled = settings.isDiagnosticLoggingEnabled
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventType = event.eventType
        // Only process relevant window content/state transitions
        if (eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {
            return
        }

        val packageName = event.packageName
        val rawRoot: AccessibilityNodeInfo? = rootInActiveWindow ?: event.source

        if (rawRoot != null) {
            val wrappedRoot = AndroidAccessibilityNodeWrapper(rawRoot)
            try {
                eventProcessor.processEvent(
                    packageName = packageName,
                    eventType = eventType,
                    rootNode = wrappedRoot
                )
            } finally {
                // Release node references safely
                wrappedRoot.recycle()
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted by Android framework
        eventProcessor.resetDebounce()
    }

    override fun onDestroy() {
        super.onDestroy()
        settingsJob?.cancel()
    }
}
