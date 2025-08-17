package nl.thijsnissen.worklog.ports.outgoing

import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicReference
import nl.thijsnissen.worklog.domain.TimeEntry

class TogglTrackClientMock(
    val ref: AtomicReference<List<TimeEntry>> = AtomicReference(emptyList())
) : TogglTrackClient {
    override suspend fun getTimeEntriesInRange(
        startInclusive: LocalDateTime,
        endExclusive: LocalDateTime,
    ): List<TimeEntry> = ref.get()

    fun set(result: List<TimeEntry>) = ref.set(result)

    fun reset() = ref.set(emptyList())
}
