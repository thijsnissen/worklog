package nl.thijsnissen.worklog.domain

import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID

data class Worklog(
    val id: UUID,
    val issueId: IssueId,
    val issueKey: IssueKey,
    val timeEntry: TimeEntry,
    val isExported: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val hash: Hash = hash(issueId, timeEntry.startInclusive, timeEntry.endInclusive)

    companion object {
        fun hash(
            issueId: IssueId,
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
        ): Hash =
            MessageDigest.getInstance("SHA-256")
                .digest("${issueId.value}|${startInclusive}|${endInclusive}".toByteArray())
                .let(Base64.getEncoder()::encodeToString)
                .let(::Hash)
    }
}
