package nl.thijsnissen.worklog.domain

import java.util.UUID

sealed interface WorklogResult {
    data object Success : WorklogResult

    data class WorklogsSuccess(val worklogs: List<Worklog>) : WorklogResult

    data class ImportInRangeSuccess(val rowsAffected: Long) : WorklogResult

    data class ExportByIdsSuccess(val rowsAffected: Long) : WorklogResult

    data class DeleteByIdsSuccess(val rowsAffected: Long) : WorklogResult

    data class IdsNotFoundError(val ids: List<UUID>) : WorklogResult

    data class IssueKeysNotFoundError(val entries: List<IssueKey>) : WorklogResult

    data class InvalidDateTimeRangeError(val message: String) : WorklogResult

    data object NoTimeEntriesFoundError : WorklogResult
}
