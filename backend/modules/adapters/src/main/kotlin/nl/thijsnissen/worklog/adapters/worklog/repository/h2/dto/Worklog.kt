package nl.thijsnissen.worklog.adapters.worklog.repository.h2.dto

import io.r2dbc.spi.Readable
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID
import nl.thijsnissen.worklog.domain.Description
import nl.thijsnissen.worklog.domain.IssueId
import nl.thijsnissen.worklog.domain.IssueKey
import nl.thijsnissen.worklog.domain.TimeEntry
import nl.thijsnissen.worklog.domain.Worklog as DomainWorklog

data class Worklog(
    val id: UUID,
    val issueId: Long,
    val issueKey: String,
    val startInclusive: LocalDateTime,
    val endInclusive: LocalDateTime,
    val durationSeconds: Long,
    val description: String,
    val isExported: Boolean,
    val hash: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun toDomain(): DomainWorklog =
        DomainWorklog(
            id = id,
            issueId = IssueId(issueId),
            issueKey = IssueKey(issueKey),
            timeEntry =
                TimeEntry(
                    startInclusive = startInclusive,
                    endInclusive = endInclusive,
                    duration = Duration.ofSeconds(durationSeconds),
                    description = Description(description),
                ),
            createdAt = createdAt,
            updatedAt = updatedAt,
            isExported = isExported,
        )

    companion object {
        fun fromReadable(row: Readable): Worklog =
            Worklog(
                id = row["id"] as UUID,
                issueId = row["issue_id"] as Long,
                issueKey = row["issue_key"] as String,
                startInclusive = row["start_inclusive"] as LocalDateTime,
                endInclusive = row["end_inclusive"] as LocalDateTime,
                durationSeconds = row["duration_seconds"] as Long,
                description = row["description"] as String,
                isExported = row["is_exported"] as Boolean,
                hash = row["hash"] as String,
                createdAt = (row["created_at"] as OffsetDateTime).toInstant(),
                updatedAt = (row["updated_at"] as OffsetDateTime).toInstant(),
            )
    }
}
