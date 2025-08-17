package nl.thijsnissen.worklog.ports.outgoing

import java.util.concurrent.atomic.AtomicReference
import nl.thijsnissen.worklog.domain.IssueId
import nl.thijsnissen.worklog.domain.IssueKey

class JiraClientMock(
    val ref: AtomicReference<Map<IssueKey, IssueId>> = AtomicReference(emptyMap())
) : JiraClient {
    override suspend fun getIssueIds(issueKeys: List<IssueKey>): Map<IssueKey, IssueId> = ref.get()

    fun set(result: Map<IssueKey, IssueId>) = ref.set(result)

    fun reset() = ref.set(emptyMap())
}
