package nl.thijsnissen.worklog.ports.outgoing

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import nl.thijsnissen.worklog.domain.Worklog

class TempoClientMock(val ref: AtomicReference<List<Worklog>> = AtomicReference(emptyList())) :
    TempoClient {
    override suspend fun send(worklogs: List<Worklog>): List<UUID> =
        ref.set(worklogs).let { get().map { it.id } }

    fun get(): List<Worklog> = ref.get()

    fun reset() = ref.set(emptyList())
}
