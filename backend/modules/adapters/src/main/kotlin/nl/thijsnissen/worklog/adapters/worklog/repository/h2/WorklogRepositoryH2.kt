package nl.thijsnissen.worklog.adapters.worklog.repository.h2

import java.util.UUID
import kotlin.collections.emptyMap
import kotlinx.coroutines.reactor.awaitSingle
import nl.thijsnissen.worklog.adapters.worklog.repository.h2.dto.Worklog as WorklogDto
import nl.thijsnissen.worklog.domain.Hash
import nl.thijsnissen.worklog.domain.Worklog
import nl.thijsnissen.worklog.ports.outgoing.WorklogRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await

class WorklogRepositoryH2(val config: WorklogRepositoryH2Config, val client: DatabaseClient) :
    WorklogRepository {
    override suspend fun upsertAll(worklogs: List<Worklog>): Long {
        val params =
            worklogs.foldIndexed(emptyMap<String, Any>()) { i, acc, w ->
                acc +
                    ("id$i" to w.id) +
                    ("issueId$i" to w.issueId.value) +
                    ("issueKey$i" to w.issueKey.value) +
                    ("startInclusive$i" to w.timeEntry.startInclusive) +
                    ("endInclusive$i" to w.timeEntry.endInclusive) +
                    ("durationSeconds$i" to w.timeEntry.duration.toSeconds()) +
                    ("description$i" to w.timeEntry.description.value) +
                    ("hash$i" to w.hash.value) +
                    ("isExported$i" to w.isExported) +
                    ("createdAt$i" to w.createdAt) +
                    ("updatedAt$i" to w.updatedAt)
            }

        return client
            .sql(upsertAllQuery(config.tableName, worklogs))
            .bindValues(params)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun getAll(): List<Worklog> =
        client
            .sql(getAllQuery(config.tableName))
            .map { WorklogDto.fromReadable(it).toDomain() }
            .all()
            .collectList()
            .awaitSingle()

    override suspend fun getByIds(ids: List<UUID>): List<Worklog> {
        val params = ids.mapIndexed { i, id -> "id$i" to id }.toMap()

        return client
            .sql(getByIdsQuery(config.tableName, ids))
            .bindValues(params)
            .map { WorklogDto.fromReadable(it).toDomain() }
            .all()
            .collectList()
            .awaitSingle()
    }

    override suspend fun getByHashes(hashes: List<Hash>): List<Worklog> {
        val params = hashes.mapIndexed { i, hash -> "hash$i" to hash.value }.toMap()

        return client
            .sql(getByHashesQuery(config.tableName, hashes))
            .bindValues(params)
            .map { WorklogDto.fromReadable(it).toDomain() }
            .all()
            .collectList()
            .awaitSingle()
    }

    override suspend fun setIsExportedByIds(ids: List<UUID>): Long {
        val params = ids.mapIndexed { i, id -> "id$i" to id }.toMap() + ("isExported" to true)

        return client
            .sql(setIsExportedByIdsQuery(config.tableName, ids))
            .bindValues(params)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun deleteByIds(ids: List<UUID>): Long {
        val params = ids.mapIndexed { i, id -> "id$i" to id }.toMap()

        return client
            .sql(deleteByIdsQuery(config.tableName, ids))
            .bindValues(params)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun truncate() = client.sql(truncateQuery(config.tableName)).await()

    companion object {
        private fun upsertAllQuery(tableName: String, worklogs: List<Worklog>): String {
            val values =
                worklogs.indices.joinToString(", \n") { i ->
                    "(:id$i, :issueId$i, :issueKey$i, :startInclusive$i, :endInclusive$i, :durationSeconds$i," +
                        ":description$i, :hash$i, :isExported$i, :createdAt$i, :updatedAt$i)"
                }

            return """
                MERGE INTO $tableName
                USING (VALUES $values)
                AS source (
                    id,
                    issue_id,
                    issue_key,
                    start_inclusive,
                    end_inclusive,
                    duration_seconds,
                    description,
                    hash,
                    is_exported,
                    created_at,
                    updated_at
                )
                ON $tableName.id = source.id
                WHEN MATCHED THEN UPDATE SET
                    issue_id = source.issue_id,
                    issue_key = source.issue_key,
                    start_inclusive = source.start_inclusive,
                    end_inclusive = source.end_inclusive,
                    duration_seconds = source.duration_seconds,
                    description = source.description,
                    hash = source.hash,
                    is_exported = source.is_exported,
                    updated_at = source.updated_at
                WHEN NOT MATCHED THEN INSERT (
                    id,
                    issue_id,
                    issue_key,
                    start_inclusive,
                    end_inclusive,
                    duration_seconds,
                    description,
                    hash,
                    is_exported,
                    created_at,
                    updated_at
                ) VALUES (
                    source.id,
                    source.issue_id,
                    source.issue_key,
                    source.start_inclusive,
                    source.end_inclusive,
                    source.duration_seconds,
                    source.description,
                    source.hash,
                    source.is_exported,
                    source.created_at,
                    source.updated_at
                )
            """
                .trimIndent()
        }

        private fun getAllQuery(tableName: String): String = "SELECT * FROM $tableName"

        private fun getByIdsQuery(tableName: String, ids: List<UUID>): String =
            "SELECT * FROM $tableName WHERE id IN (${ids.indices.joinToString(", ") { ":id$it" }})"

        private fun getByHashesQuery(tableName: String, hashes: List<Hash>): String =
            "SELECT * FROM $tableName WHERE hash IN (${hashes.indices.joinToString(", ") { ":hash$it" }})"

        private fun setIsExportedByIdsQuery(tableName: String, ids: List<UUID>): String =
            "UPDATE $tableName SET is_exported = :isExported WHERE id IN (${ids.indices.joinToString(", ") { ":id$it" }})"

        private fun deleteByIdsQuery(tableName: String, ids: List<UUID>): String =
            "DELETE from $tableName WHERE id IN (${ids.indices.joinToString(", ") { ":id$it" }})"

        private fun truncateQuery(tableName: String): String = "TRUNCATE TABLE $tableName"
    }
}
