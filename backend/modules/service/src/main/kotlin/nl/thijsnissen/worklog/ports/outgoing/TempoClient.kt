package nl.thijsnissen.worklog.ports.outgoing

import java.util.*
import nl.thijsnissen.worklog.domain.Worklog

interface TempoClient {
    suspend fun send(worklogs: List<Worklog>): List<UUID>
}
