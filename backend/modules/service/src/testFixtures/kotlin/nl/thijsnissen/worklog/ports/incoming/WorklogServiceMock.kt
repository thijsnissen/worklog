package nl.thijsnissen.worklog.ports.incoming

import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import nl.thijsnissen.worklog.domain.WorklogResult

class WorklogServiceMock(
    val worklogsRef: AtomicReference<WorklogResult> = AtomicReference(WorklogResult.Success),
    val errorRef: AtomicReference<Exception?> = AtomicReference(null),
) : WorklogService {
    override suspend fun getAll(): WorklogResult = result()

    override suspend fun importInRange(
        startInclusive: LocalDateTime,
        endInclusive: LocalDateTime,
    ): WorklogResult = result()

    override suspend fun exportByIds(ids: List<UUID>): WorklogResult = result()

    override suspend fun deleteByIds(ids: List<UUID>): WorklogResult = result()

    override suspend fun flush(): WorklogResult = result()

    private fun result(): WorklogResult {
        errorRef.get()?.let { throw it }

        return worklogsRef.get()
    }

    fun set(worklogs: WorklogResult) = worklogsRef.set(worklogs)

    fun error(exception: Exception) = errorRef.set(exception)

    fun reset() {
        worklogsRef.set(WorklogResult.Success)
        errorRef.set(null)
    }
}
