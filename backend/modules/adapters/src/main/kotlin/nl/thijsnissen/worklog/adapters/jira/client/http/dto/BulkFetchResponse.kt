package nl.thijsnissen.worklog.adapters.jira.client.http.dto

import nl.thijsnissen.worklog.domain.IssueId
import nl.thijsnissen.worklog.domain.IssueKey

data class BulkFetchResponse(val issues: List<Response>) {
    fun toDomain(): Map<IssueKey, IssueId> = issues.associate { it.toDomain() }
}

data class Response(val key: String, val id: Long) {
    fun toDomain(): Pair<IssueKey, IssueId> = IssueKey(value = key) to IssueId(value = id)
}
