package nl.thijsnissen.worklog.adapters.tempo.client.http.dto

import java.time.LocalDate
import java.time.LocalTime
import nl.thijsnissen.worklog.domain.TimeEntry

typealias BulkRequest = List<Request>

data class Request(
    val authorAccountId: String,
    val startDate: LocalDate,
    val startTime: LocalTime,
    val timeSpentSeconds: Long,
    val description: String,
) {
    companion object {
        fun List<TimeEntry>.fromDomain(authorAccountId: String): BulkRequest =
            this.map { fromDomain(authorAccountId, it) }

        fun fromDomain(authorAccountId: String, timeEntry: TimeEntry): Request =
            Request(
                authorAccountId = authorAccountId,
                startDate = timeEntry.startInclusive.toLocalDate(),
                startTime = timeEntry.startInclusive.toLocalTime(),
                timeSpentSeconds = timeEntry.duration.seconds,
                description = timeEntry.description.value,
            )
    }
}
