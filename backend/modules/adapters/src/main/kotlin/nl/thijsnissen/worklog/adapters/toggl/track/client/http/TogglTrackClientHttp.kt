package nl.thijsnissen.worklog.adapters.toggl.track.client.http

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.dto.InRangeResponse
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.dto.Response.Companion.toDomain
import nl.thijsnissen.worklog.domain.TimeEntry
import nl.thijsnissen.worklog.ports.outgoing.TogglTrackClient
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class TogglTrackClientHttp(val config: TogglTrackClientHttpConfig, val client: WebClient) :
    TogglTrackClient {
    override suspend fun getTimeEntriesInRange(
        startInclusive: LocalDateTime,
        endExclusive: LocalDateTime,
    ): List<TimeEntry> =
        client
            .get()
            .uri(config.host.resolve(
                "me/time_entries?start_date=${
                    startInclusive.toRFC3339(config.timeZone)
                }&end_date=${
                    endExclusive.toRFC3339(config.timeZone)
                }"
            ))
            .headers { it.setBasicAuth(config.apiToken, "api_token") }
            .retrieve()
            .awaitBody<InRangeResponse>()
            .toDomain(config.timeZone)

    companion object {
        fun LocalDateTime.toRFC3339(zoneId: ZoneId): String =
            this.atZone(zoneId)
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT)
    }
}
