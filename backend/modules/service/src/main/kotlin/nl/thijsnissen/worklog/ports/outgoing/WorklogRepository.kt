package nl.thijsnissen.worklog.ports.outgoing

import java.util.UUID
import nl.thijsnissen.worklog.domain.Hash
import nl.thijsnissen.worklog.domain.Worklog

interface WorklogRepository {
    suspend fun upsertAll(worklogs: List<Worklog>): Long

    suspend fun getAll(): List<Worklog>

    suspend fun getByIds(ids: List<UUID>): List<Worklog>

    suspend fun getByHashes(hashes: List<Hash>): List<Worklog>

    suspend fun setIsExportedByIds(ids: List<UUID>): Long

    suspend fun deleteByIds(ids: List<UUID>): Long

    suspend fun truncate()
}
