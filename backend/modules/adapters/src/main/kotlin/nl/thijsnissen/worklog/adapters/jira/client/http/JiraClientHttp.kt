package nl.thijsnissen.worklog.adapters.jira.client.http

import nl.thijsnissen.worklog.adapters.jira.client.http.dto.BulkFetchRequest
import nl.thijsnissen.worklog.adapters.jira.client.http.dto.BulkFetchResponse
import nl.thijsnissen.worklog.domain.IssueId
import nl.thijsnissen.worklog.domain.IssueKey
import nl.thijsnissen.worklog.ports.outgoing.JiraClient
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class JiraClientHttp(val config: JiraClientHttpConfig, val client: WebClient) : JiraClient {
    override suspend fun getIssueIds(issueKeys: List<IssueKey>): Map<IssueKey, IssueId> =
        client
            .post()
            .uri(config.host.resolve("issue/bulkfetch"))
            .headers { it.setBasicAuth(config.userEmail, config.apiKey) }
            .bodyValue(BulkFetchRequest(issueIdsOrKeys = issueKeys.map { it.value }))
            .retrieve()
            .awaitBody<BulkFetchResponse>()
            .toDomain()
}
