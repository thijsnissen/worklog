package nl.thijsnissen.worklog.adapters.tempo.client.http

import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import nl.thijsnissen.worklog.adapters.tempo.client.http.dto.BulkResponse
import nl.thijsnissen.worklog.adapters.tempo.client.http.dto.Request.Companion.fromDomain
import nl.thijsnissen.worklog.adapters.tempo.client.http.dto.Response.Companion.toDomain
import nl.thijsnissen.worklog.domain.IssueId
import nl.thijsnissen.worklog.domain.TimeEntry
import nl.thijsnissen.worklog.domain.Worklog
import nl.thijsnissen.worklog.ports.outgoing.TempoClient
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class TempoClientHttp(val config: TempoClientHttpConfig, val client: WebClient) : TempoClient {
    override suspend fun send(worklogs: List<Worklog>): List<UUID> {
        val groupedWorklogs = worklogs.groupBy(Worklog::issueId)

        val sendIssueIds = coroutineScope {
            groupedWorklogs
                .map { (issueId, worklogs) ->
                    async { request(issueId, worklogs.map { it.timeEntry }).firstOrNull() }
                }
                .awaitAll()
                .filterNotNull()
        }

        return sendIssueIds.flatMap { groupedWorklogs[it].orEmpty().map { it.id } }
    }

    private suspend fun request(issueId: IssueId, entries: List<TimeEntry>): List<IssueId> =
        Semaphore(permits = config.maxConcurrentRequests).withPermit {
            client
                .post()
                .uri(config.host.resolve("worklogs/issue/${issueId.value}/bulk"))
                .headers { it.setBearerAuth(config.apiKey) }
                .bodyValue(entries.fromDomain(config.accountId))
                .retrieve()
                .awaitBody<BulkResponse>()
                .toDomain()
        }
}
