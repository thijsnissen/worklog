package nl.thijsnissen.worklog.ports.incoming

import java.time.LocalDateTime
import java.util.UUID
import nl.thijsnissen.worklog.domain.WorklogResult

interface WorklogService {
    suspend fun getAll(): WorklogResult

    suspend fun importInRange(
        startInclusive: LocalDateTime,
        endInclusive: LocalDateTime,
    ): WorklogResult

    suspend fun exportByIds(ids: List<UUID>): WorklogResult

    suspend fun deleteByIds(ids: List<UUID>): WorklogResult

    suspend fun flush(): WorklogResult
}
