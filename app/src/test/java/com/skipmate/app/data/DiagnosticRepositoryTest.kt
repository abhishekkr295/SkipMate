package com.skipmate.app.data

import com.skipmate.app.data.diagnostics.DiagnosticEvent
import com.skipmate.app.data.diagnostics.DiagnosticRepository
import com.skipmate.app.data.diagnostics.DiagnosticStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiagnosticRepositoryTest {

    @Test
    fun testRecordAndCapacityLimit() {
        val repo = DiagnosticRepository(maxCapacity = 3)

        repo.recordEvent(DiagnosticEvent(status = DiagnosticStatus.TARGET_APP_DETECTED, summary = "Event 1"))
        repo.recordEvent(DiagnosticEvent(status = DiagnosticStatus.NO_CANDIDATE_FOUND, summary = "Event 2"))
        repo.recordEvent(DiagnosticEvent(status = DiagnosticStatus.CLICK_ATTEMPTED, summary = "Event 3"))

        assertEquals(3, repo.getEventCount())
        assertEquals("Event 3", repo.latestEvent.value?.summary)

        // Add 4th event, pushing out Event 1
        repo.recordEvent(DiagnosticEvent(status = DiagnosticStatus.CLICK_DISPATCH_SUCCESS, summary = "Event 4"))

        assertEquals(3, repo.getEventCount())
        assertEquals("Event 4", repo.latestEvent.value?.summary)
        val summaries = repo.events.value.map { it.summary }
        assertEquals(listOf("Event 4", "Event 3", "Event 2"), summaries)
    }

    @Test
    fun testClearEvents() {
        val repo = DiagnosticRepository()
        repo.recordEvent(DiagnosticEvent(status = DiagnosticStatus.CLICK_ATTEMPTED, summary = "Event 1"))
        assertEquals(1, repo.getEventCount())

        repo.clearEvents()
        assertEquals(0, repo.getEventCount())
        assertNull(repo.latestEvent.value)
        assertNull(repo.lastSuccessfulSkip.value)
        assertEquals(0, repo.totalSkipsCount.value)
    }

    @Test
    fun testSuccessfulSkipTracking() {
        val repo = DiagnosticRepository()
        assertNull(repo.lastSuccessfulSkip.value)
        assertEquals(0, repo.totalSkipsCount.value)

        val successEvent1 = DiagnosticEvent(
            status = DiagnosticStatus.CLICK_DISPATCH_SUCCESS,
            summary = "Ad 1 skipped"
        )
        repo.recordEvent(successEvent1)
        assertEquals("Ad 1 skipped", repo.lastSuccessfulSkip.value?.summary)
        assertEquals(1, repo.totalSkipsCount.value)

        // Routine background scan should NOT erase lastSuccessfulSkip
        repo.recordEvent(DiagnosticEvent(status = DiagnosticStatus.NO_CANDIDATE_FOUND, summary = "Scanning"))
        assertEquals("Ad 1 skipped", repo.lastSuccessfulSkip.value?.summary)
        assertEquals(1, repo.totalSkipsCount.value)

        // Second successful skip
        val successEvent2 = DiagnosticEvent(
            status = DiagnosticStatus.CLICK_DISPATCH_SUCCESS,
            summary = "Ad 2 skipped"
        )
        repo.recordEvent(successEvent2)
        assertEquals("Ad 2 skipped", repo.lastSuccessfulSkip.value?.summary)
        assertEquals(2, repo.totalSkipsCount.value)
    }
}
