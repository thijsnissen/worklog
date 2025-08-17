package nl.thijsnissen.worklog.ports.outgoing

import java.time.LocalDateTime
import nl.thijsnissen.worklog.domain.TimeEntry

interface TogglTrackClient {
    suspend fun getTimeEntriesInRange(
        startInclusive: LocalDateTime,
        endExclusive: LocalDateTime,
    ): List<TimeEntry>
}
