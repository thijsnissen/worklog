package nl.thijsnissen.worklog.adapters.toggl.track.client.http.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import nl.thijsnissen.worklog.domain.Description
import nl.thijsnissen.worklog.domain.TimeEntry

typealias InRangeResponse = List<Response>

data class Response(
    val start: OffsetDateTime,
    val stop: OffsetDateTime,
    @param:JsonProperty(required = true) val duration: Long,
    val description: String,
) {
    fun toDomain(timeZone: ZoneId): TimeEntry =
        TimeEntry(
            startInclusive = start.toLocalDateTime(to = timeZone),
            endInclusive = stop.toLocalDateTime(to = timeZone),
            duration = Duration.ofSeconds(duration),
            description = Description(description),
        )

    companion object {
        fun OffsetDateTime.toLocalDateTime(to: ZoneId): LocalDateTime =
            this.atZoneSameInstant(to).toLocalDateTime()

        fun LocalDateTime.toOffsetDateTime(from: ZoneId, to: ZoneId): OffsetDateTime =
            this.atZone(from).withZoneSameInstant(to).toOffsetDateTime()

        fun InRangeResponse.toDomain(timeZone: ZoneId): List<TimeEntry> =
            this.map { it.toDomain(timeZone) }
    }
}
