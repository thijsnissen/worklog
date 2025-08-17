package nl.thijsnissen.worklog.adapters.tempo.client.http.dto

import nl.thijsnissen.worklog.domain.IssueId

typealias BulkResponse = List<Response>

data class Response(val issue: Issue) {
    fun toDomain(): IssueId = issue.toDomain()

    companion object {
        fun BulkResponse.toDomain(): List<IssueId> = this.map { it.toDomain() }
    }
}

data class Issue(val id: Long) {
    fun toDomain(): IssueId = IssueId(id)
}
