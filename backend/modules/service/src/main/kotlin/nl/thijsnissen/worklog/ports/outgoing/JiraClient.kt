package nl.thijsnissen.worklog.ports.outgoing

import nl.thijsnissen.worklog.domain.IssueId
import nl.thijsnissen.worklog.domain.IssueKey

interface JiraClient {
    suspend fun getIssueIds(issueKeys: List<IssueKey>): Map<IssueKey, IssueId>
}
