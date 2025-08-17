package nl.thijsnissen.worklog.core

import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import nl.thijsnissen.worklog.domain.Description
import nl.thijsnissen.worklog.domain.IssueId
import nl.thijsnissen.worklog.domain.IssueKey
import nl.thijsnissen.worklog.domain.TimeEntry
import nl.thijsnissen.worklog.domain.Worklog
import nl.thijsnissen.worklog.domain.WorklogResult
import nl.thijsnissen.worklog.ports.incoming.WorklogService
import nl.thijsnissen.worklog.ports.outgoing.JiraClient
import nl.thijsnissen.worklog.ports.outgoing.TempoClient
import nl.thijsnissen.worklog.ports.outgoing.TogglTrackClient
import nl.thijsnissen.worklog.ports.outgoing.WorklogRepository

class WorklogServiceImpl(
    val jiraClient: JiraClient,
    val repository: WorklogRepository,
    val tempoClient: TempoClient,
    val togglTrackClient: TogglTrackClient,
    val config: WorklogServiceImplConfig,
) : WorklogService {
    override suspend fun getAll(): WorklogResult =
        repository.getAll().let(WorklogResult::WorklogsSuccess)

    override suspend fun importInRange(
        startInclusive: LocalDateTime,
        endInclusive: LocalDateTime,
    ): WorklogResult =
        when {
            startInclusive.isEqual(endInclusive) ->
                WorklogResult.InvalidDateTimeRangeError(
                    "startInclusive cannot be equal to endInclusive"
                )
            startInclusive.isAfter(endInclusive) ->
                WorklogResult.InvalidDateTimeRangeError(
                    "startInclusive cannot be greater than endInclusive"
                )
            else -> importTimeEntries(startInclusive, endInclusive)
        }

    private suspend fun importTimeEntries(
        startInclusive: LocalDateTime,
        endInclusive: LocalDateTime,
    ): WorklogResult {
        val entries =
            togglTrackClient
                .getTimeEntriesInRange(startInclusive, endInclusive)
                .groupBy {
                    IssueKey(it.description.value.substringBefore(config.delimiter).normalize())
                }
                .mapValues { (_, entries) ->
                    entries.map {
                        it.copy(
                            description =
                                Description(
                                    it.description.value.substringAfter(config.delimiter).trim()
                                )
                        )
                    }
                }

        return when {
            entries.isEmpty() -> WorklogResult.NoTimeEntriesFoundError
            else -> importIssueKeysIds(entries)
        }
    }

    private suspend fun importIssueKeysIds(entries: Map<IssueKey, List<TimeEntry>>): WorklogResult {
        val issueKeysIds = jiraClient.getIssueIds(entries.keys.toList())

        val notFound =
            entries.keys.filterNot { issueKey -> issueKeysIds.keys.any { it eqv issueKey } }

        return when {
            notFound.isNotEmpty() -> WorklogResult.IssueKeysNotFoundError(notFound)
            else -> importProcess(entries, issueKeysIds)
        }
    }

    private suspend fun importProcess(
        entries: Map<IssueKey, List<TimeEntry>>,
        issueKeysIds: Map<IssueKey, IssueId>,
    ): WorklogResult {
        val existing =
            issueKeysIds
                .flatMap { (issueKey, issueId) ->
                    entries.findNormalized(issueKey).map { timeEntry ->
                        Worklog.hash(issueId, timeEntry.startInclusive, timeEntry.endInclusive)
                    }
                }
                .let { repository.getByHashes(it) }
                .associateBy { it.hash }

        return issueKeysIds
            .flatMap { (issueKey, issueId) ->
                entries.findNormalized(issueKey).map { timeEntry ->
                    existing[
                            Worklog.hash(issueId, timeEntry.startInclusive, timeEntry.endInclusive)]
                        ?.copy(
                            issueKey = issueKey,
                            timeEntry = timeEntry,
                            updatedAt = Instant.now(),
                        )
                        ?: Worklog(
                            id = UUID.randomUUID(),
                            issueId = issueId,
                            issueKey = issueKey,
                            timeEntry = timeEntry,
                            createdAt = Instant.now(),
                            updatedAt = Instant.now(),
                            isExported = false,
                        )
                }
            }
            .let { repository.upsertAll(it) }
            .let { WorklogResult.ImportInRangeSuccess(it) }
    }

    override suspend fun exportByIds(ids: List<UUID>): WorklogResult {
        val entries = repository.getByIds(ids)
        val notFound = ids.filterNot { id -> entries.any { it.id == id } }

        return when {
            notFound.isNotEmpty() -> WorklogResult.IdsNotFoundError(notFound)
            else ->
                tempoClient
                    .send(entries)
                    .let { repository.setIsExportedByIds(it) }
                    .let { WorklogResult.ExportByIdsSuccess(it) }
        }
    }

    override suspend fun deleteByIds(ids: List<UUID>): WorklogResult {
        val entries = repository.getByIds(ids)
        val notFound = ids.filterNot { id -> entries.any { it.id == id } }

        return when {
            notFound.isNotEmpty() -> WorklogResult.IdsNotFoundError(notFound)
            else -> repository.deleteByIds(ids).let { WorklogResult.DeleteByIdsSuccess(it) }
        }
    }

    override suspend fun flush(): WorklogResult =
        repository.truncate().let { WorklogResult.Success }

    companion object {
        fun String.normalize(): String = this.trim().lowercase()

        fun Map<IssueKey, List<TimeEntry>>.findNormalized(key: IssueKey): List<TimeEntry> =
            this[IssueKey(key.value.normalize())] ?: emptyList()
    }
}
