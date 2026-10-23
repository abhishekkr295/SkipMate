package com.skipmate.app.data.diagnostics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedDeque

class DiagnosticRepository(
    private val maxCapacity: Int = 100
) {
    private val eventQueue = ConcurrentLinkedDeque<DiagnosticEvent>()
    private val _events = MutableStateFlow<List<DiagnosticEvent>>(emptyList())
    val events: StateFlow<List<DiagnosticEvent>> = _events.asStateFlow()

    private val _latestEvent = MutableStateFlow<DiagnosticEvent?>(null)
    val latestEvent: StateFlow<DiagnosticEvent?> = _latestEvent.asStateFlow()

    private val _lastSuccessfulSkip = MutableStateFlow<DiagnosticEvent?>(null)
    val lastSuccessfulSkip: StateFlow<DiagnosticEvent?> = _lastSuccessfulSkip.asStateFlow()

    private val _totalSkipsCount = MutableStateFlow<Int>(0)
    val totalSkipsCount: StateFlow<Int> = _totalSkipsCount.asStateFlow()

    private val _lastSignificantEvent = MutableStateFlow<DiagnosticEvent?>(null)
    val lastSignificantEvent: StateFlow<DiagnosticEvent?> = _lastSignificantEvent.asStateFlow()

    fun recordEvent(event: DiagnosticEvent) {
        eventQueue.addFirst(event)
        while (eventQueue.size > maxCapacity) {
            eventQueue.pollLast()
        }
        val currentList = eventQueue.toList()
        _events.value = currentList
        _latestEvent.value = event

        when (event.status) {
            DiagnosticStatus.CLICK_DISPATCH_SUCCESS -> {
                _lastSuccessfulSkip.value = event
                _totalSkipsCount.value += 1
                _lastSignificantEvent.value = event
            }
            DiagnosticStatus.CLICK_ATTEMPTED,
            DiagnosticStatus.CLICK_DISPATCH_FAILED -> {
                _lastSignificantEvent.value = event
            }
            else -> { /* Background frame scan or playback state */ }
        }
    }

    fun clearEvents() {
        eventQueue.clear()
        _events.value = emptyList()
        _latestEvent.value = null
        _lastSuccessfulSkip.value = null
        _totalSkipsCount.value = 0
        _lastSignificantEvent.value = null
    }

    fun getEventCount(): Int = eventQueue.size
}
