package nl.thijsnissen.worklog.adapters.api.http.worklog.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID
import nl.thijsnissen.worklog.domain.Worklog as DomainWorklog

data class Worklog(
    @field:NotNull val id: UUID,
    @field:NotNull val issueId: Long,
    @field:NotBlank val issueKey: String,
    @field:NotNull val startInclusive: LocalDateTime,
    @field:NotNull val endInclusive: LocalDateTime,
    @field:NotNull val durationSeconds: Long,
    @field:NotNull val description: String,
    @field:NotNull val exported: Boolean,
) {
    companion object {
        fun fromDomain(worklog: DomainWorklog): Worklog =
            Worklog(
                id = worklog.id,
                issueId = worklog.issueId.value,
                issueKey = worklog.issueKey.value,
                startInclusive = worklog.timeEntry.startInclusive,
                endInclusive = worklog.timeEntry.endInclusive,
                durationSeconds = worklog.timeEntry.duration.seconds,
                description = worklog.timeEntry.description.value,
                exported = worklog.isExported,
            )
    }
}
