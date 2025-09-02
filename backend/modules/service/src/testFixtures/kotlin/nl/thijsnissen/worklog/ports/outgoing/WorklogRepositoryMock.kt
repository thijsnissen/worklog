package nl.thijsnissen.worklog.ports.outgoing

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import nl.thijsnissen.worklog.domain.Hash
import nl.thijsnissen.worklog.domain.Worklog

class WorklogRepositoryMock(
    val ref: AtomicReference<Map<UUID, Worklog>> = AtomicReference(emptyMap())
) : WorklogRepository {
    override suspend fun upsertAll(worklogs: List<Worklog>): Long =
        ref.updateAndGet { it + worklogs.associateBy { it.id } }.size.toLong()

    override suspend fun getAll(): List<Worklog> = ref.get().values.toList()

    override suspend fun getByIds(ids: List<UUID>): List<Worklog> =
        ref.get().filterKeys { ids.contains(it) }.values.toList()

    override suspend fun setIsExportedByIds(ids: List<UUID>): Long =
        ref.updateAndGet {
                it.mapValues { (id, w) -> if (ids.contains(id)) w.copy(isExported = true) else w }
            }
            .size
            .toLong()

    override suspend fun getByHashes(hashes: List<Hash>): List<Worklog> =
        ref.get().filterValues { hashes.contains(it.hash) }.values.toList()

    override suspend fun deleteByIds(ids: List<UUID>): Long =
        ref.getAndUpdate { it.filterValues { !ids.contains(it.id) } }.size.toLong() -
            ref.get().size.toLong()

    override suspend fun truncate() = ref.set(emptyMap())
}
