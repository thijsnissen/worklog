package nl.thijsnissen.worklog

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.random.Random
import nl.thijsnissen.worklog.domain.Description
import nl.thijsnissen.worklog.domain.Hash
import nl.thijsnissen.worklog.domain.IssueId
import nl.thijsnissen.worklog.domain.IssueKey
import nl.thijsnissen.worklog.domain.TimeEntry
import nl.thijsnissen.worklog.domain.Worklog

data class TestData(
    val worklogs: List<Worklog>,
    val ids: List<UUID>,
    val hashes: List<Hash> = worklogs.map { it.hash },
    val timeEntries: List<TimeEntry>,
    val timeEntriesWithIssueKeys: List<TimeEntry>,
    val issueKeys: List<IssueKey>,
    val issueIds: List<IssueId>,
    val issueKeysIssueIds: Map<IssueKey, IssueId> = issueKeys.zip(issueIds).toMap(),
    val startInclusive: LocalDateTime = timeEntriesWithIssueKeys.minOf { it.startInclusive },
    val endInclusive: LocalDateTime = timeEntriesWithIssueKeys.maxOf { it.endInclusive },
    val isExported: List<Boolean>,
    val createdAt: List<Instant>,
    val updatedAt: List<Instant>,
    val size: Int,
) {
    fun partition(): Pair<TestData, TestData> {
        val split = randomInt(min = 1, max = size - 1)

        return Pair(
            first =
                TestData(
                    worklogs = worklogs.take(split),
                    ids = ids.take(split),
                    timeEntries = timeEntries.take(split),
                    timeEntriesWithIssueKeys = timeEntriesWithIssueKeys.take(split),
                    issueKeys = issueKeys.take(split),
                    issueIds = issueIds.take(split),
                    isExported = isExported.take(split),
                    createdAt = createdAt.take(split),
                    updatedAt = updatedAt.take(split),
                    size = split,
                ),
            second =
                TestData(
                    worklogs = worklogs.drop(split),
                    ids = ids.drop(split),
                    timeEntries = timeEntries.drop(split),
                    timeEntriesWithIssueKeys = timeEntriesWithIssueKeys.drop(split),
                    issueKeys = issueKeys.drop(split),
                    issueIds = issueIds.drop(split),
                    isExported = isExported.drop(split),
                    createdAt = createdAt.drop(split),
                    updatedAt = updatedAt.drop(split),
                    size = size - split,
                ),
        )
    }

    operator fun plus(that: TestData): TestData =
        TestData(
            worklogs = this.worklogs + that.worklogs,
            ids = this.ids + that.ids,
            timeEntries = this.timeEntries + that.timeEntries,
            timeEntriesWithIssueKeys =
                this.timeEntriesWithIssueKeys + that.timeEntriesWithIssueKeys,
            issueKeys = this.issueKeys + that.issueKeys,
            issueIds = this.issueIds + that.issueIds,
            isExported = this.isExported + that.isExported,
            createdAt = this.createdAt + that.createdAt,
            updatedAt = this.updatedAt + that.updatedAt,
            size = this.size + that.size,
        )

    companion object {
        fun testCase(
            delimiter: String = ":",
            size: Int = randomInt(5, 100),
            ids: List<UUID> = randomUuids(size),
            issueKeys: List<IssueKey> = randomIssueKeys(size),
            issueIds: List<IssueId> = randomIssueIds(size),
            timeEntries: List<TimeEntry> = randomTimeEntries(size),
            isExported: List<Boolean> = randomBooleans(size),
            createdAt: List<Instant> = randomInstants(size),
            updatedAt: List<Instant> = randomInstants(size),
        ): TestData {
            val timeEntriesWithIssueKeys =
                timeEntries.zip(issueKeys) { timeEntry, issueKey ->
                    timeEntry.copy(
                        description =
                            Description(
                                "${issueKey.value}${delimiter}${timeEntry.description.value}"
                            )
                    )
                }
            val worklogs =
                (0..<size).map {
                    Worklog(
                        id = ids[it],
                        issueId = issueIds[it],
                        issueKey = issueKeys[it],
                        timeEntry = timeEntries[it],
                        isExported = isExported[it],
                        createdAt = createdAt[it],
                        updatedAt = updatedAt[it],
                    )
                }

            return TestData(
                worklogs = worklogs,
                ids = ids,
                timeEntries = timeEntries,
                timeEntriesWithIssueKeys = timeEntriesWithIssueKeys,
                issueKeys = issueKeys,
                issueIds = issueIds,
                isExported = isExported,
                createdAt = createdAt,
                updatedAt = updatedAt,
                size = size,
            )
        }

        fun randomBoolean(): Boolean = Random.nextBoolean()

        fun randomBooleans(size: Int = randomInt(1, 100)): List<Boolean> =
            List(size) { randomBoolean() }

        fun randomUuids(size: Int = randomInt(1, 100)): List<UUID> =
            List(size) { UUID.randomUUID() }

        fun randomInstant(min: Instant = Instant.MIN, max: Instant = Instant.MAX): Instant =
            min.plusSeconds(randomLong(0, ChronoUnit.SECONDS.between(min, max)))

        fun randomInstants(
            size: Int = randomInt(1, 100),
            min: Instant = Instant.MIN,
            max: Instant = Instant.MAX,
        ): List<Instant> = List(size) { randomInstant(min, max) }

        fun randomInt(min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Int =
            Random.nextInt(min, max)

        fun randomLong(min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE): Long =
            Random.nextLong(min, max)

        fun randomLongs(
            min: Long = Long.MIN_VALUE,
            max: Long = Long.MAX_VALUE,
            size: Int = randomInt(1, 100),
        ): List<Long> = List(size) { randomLong(min, max) }

        fun randomChar(): Char = (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()

        fun randomString(length: Int = 10): String = List(length) { randomChar() }.joinToString("")

        fun randomStrings(length: Int = 10, size: Int = randomInt(1, 100)): List<String> =
            List(size) { randomString(length) }

        fun randomIssueKey(): IssueKey = IssueKey(randomString())

        fun randomIssueKeys(size: Int = randomInt(1, 100)): List<IssueKey> =
            List(size) { randomIssueKey() }

        fun randomIssueId(): IssueId = IssueId(randomLong())

        fun randomIssueIds(size: Int = randomInt(1, 100)): List<IssueId> =
            List(size) { randomIssueId() }

        fun randomDescription(): Description = Description(randomString())

        fun randomTimeEntry(): TimeEntry {
            val duration = randomInt(min = 1).toLong()
            val startInclusive =
                randomLocalDateTime(max = LocalDateTime.now().minusSeconds(duration))
            val endInclusive = startInclusive.plusSeconds(duration)

            return TimeEntry(
                startInclusive = startInclusive,
                endInclusive = endInclusive,
                duration = Duration.ofSeconds(duration),
                description = randomDescription(),
            )
        }

        fun randomWorklog(
            issueId: IssueId = randomIssueId(),
            issueKey: IssueKey = randomIssueKey(),
            timeEntry: TimeEntry = randomTimeEntry(),
        ): Worklog =
            Worklog(
                id = UUID.randomUUID(),
                issueId = issueId,
                issueKey = issueKey,
                timeEntry = timeEntry,
                isExported = randomBoolean(),
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        fun randomWorklogs(size: Int = randomInt(1, 100)): List<Worklog> =
            List(size) { randomWorklog() }

        fun randomTimeEntries(size: Int = randomInt(1, 100)): List<TimeEntry> =
            List(size) { randomTimeEntry() }

        fun randomLocalDateTime(
            min: LocalDateTime = LocalDateTime.MIN,
            max: LocalDateTime = LocalDateTime.MAX,
        ): LocalDateTime = min.plusSeconds(randomLong(0, ChronoUnit.SECONDS.between(min, max)))
    }
}
