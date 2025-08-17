package nl.thijsnissen.worklog.core

import java.time.Duration
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import nl.thijsnissen.worklog.TestData
import nl.thijsnissen.worklog.TestData.Companion.randomLocalDateTime
import nl.thijsnissen.worklog.TestData.Companion.randomLong
import nl.thijsnissen.worklog.TestData.Companion.randomWorklogs
import nl.thijsnissen.worklog.assertSameElements
import nl.thijsnissen.worklog.core.WorklogServiceImpl.Companion.normalize
import nl.thijsnissen.worklog.domain.Description
import nl.thijsnissen.worklog.domain.IssueKey
import nl.thijsnissen.worklog.domain.WorklogResult
import nl.thijsnissen.worklog.ports.outgoing.JiraClientMock
import nl.thijsnissen.worklog.ports.outgoing.TempoClientMock
import nl.thijsnissen.worklog.ports.outgoing.TogglTrackClientMock
import nl.thijsnissen.worklog.ports.outgoing.WorklogRepositoryMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WorklogServiceImplTest {
    @Test
    fun getAllWorklogsSuccess() {
        val testCase = TestData.testCase()

        runTest {
            repository.upsertAll(testCase.worklogs)

            assertSameElements(
                testCase.worklogs,
                (service.getAll() as WorklogResult.WorklogsSuccess).worklogs,
            )
        }
    }

    @Test
    fun importInRangeSuccess() {
        val testCase = TestData.testCase()

        togglTrackClient.set(testCase.timeEntriesWithIssueKeys)
        jiraClient.set(testCase.issueKeysIssueIds)

        runTest {
            assertEquals(
                testCase.size.toLong(),
                (service.importInRange(testCase.startInclusive, testCase.endInclusive)
                        as WorklogResult.ImportInRangeSuccess)
                    .rowsAffected,
            )
        }
    }

    @Test
    fun importInRangeInvalidDateTimeRangeErrorIsEqual() {
        val dateTime = randomLocalDateTime()

        runTest {
            assertTrue(
                (service.importInRange(dateTime, dateTime)
                        as WorklogResult.InvalidDateTimeRangeError)
                    .message
                    .contains("cannot be equal")
            )
        }
    }

    @Test
    fun importInRangeInvalidDateTimeRangeErrorIsAfter() {
        val dateTime = randomLocalDateTime()

        runTest {
            assertTrue(
                (service.importInRange(
                        dateTime,
                        dateTime.minusSeconds(randomLong(min = 1, max = Int.MAX_VALUE.toLong())),
                    ) as WorklogResult.InvalidDateTimeRangeError)
                    .message
                    .contains("cannot be greater")
            )
        }
    }

    @Test
    fun importInRangeNoTimeEntriesFoundError() {
        val dateTime = randomLocalDateTime()

        runTest {
            assertEquals(
                WorklogResult.NoTimeEntriesFoundError,
                service.importInRange(
                    dateTime,
                    dateTime.plusSeconds(randomLong(min = 1, max = Int.MAX_VALUE.toLong())),
                ),
            )
        }
    }

    @Test
    fun importInRangeIssueKeysNotFoundError() {
        val testCase = TestData.testCase()
        val (found, notFound) = testCase.partition()

        togglTrackClient.set(testCase.timeEntriesWithIssueKeys)
        jiraClient.set(found.issueKeysIssueIds)

        runTest {
            assertSameElements(
                notFound.issueKeys.map { it.value.normalize() },
                (service.importInRange(testCase.startInclusive, testCase.endInclusive)
                        as WorklogResult.IssueKeysNotFoundError)
                    .entries
                    .map { it.value.normalize() },
            )
        }
    }

    @Test
    fun importInRangeGroupTimeEntriesByIssueKey() {
        val singleTestCase = TestData.testCase()
        val doubleTestCase = singleTestCase + singleTestCase

        togglTrackClient.set(doubleTestCase.timeEntriesWithIssueKeys)
        jiraClient.set(singleTestCase.issueKeysIssueIds)

        runTest {
            assertEquals(
                doubleTestCase.size.toLong(),
                (service.importInRange(doubleTestCase.startInclusive, doubleTestCase.endInclusive)
                        as WorklogResult.ImportInRangeSuccess)
                    .rowsAffected,
            )
        }
    }

    @Test
    fun importInRangeEqvIssueKeys() {
        val testCase = TestData.testCase()

        togglTrackClient.set(
            testCase.timeEntriesWithIssueKeys.map {
                it.copy(description = Description(" " + it.description.value.uppercase()))
            }
        )
        jiraClient.set(
            testCase.issueKeysIssueIds.mapKeys { (issueKey, _) ->
                IssueKey(issueKey.value.lowercase() + " ")
            }
        )

        runTest {
            assertEquals(
                testCase.size.toLong(),
                (service.importInRange(testCase.startInclusive, testCase.endInclusive)
                        as WorklogResult.ImportInRangeSuccess)
                    .rowsAffected,
            )
        }
    }

    @Test
    fun importInRangeUpdateExistingWorklogsByHash() {
        val testCase = TestData.testCase()

        togglTrackClient.set(testCase.timeEntriesWithIssueKeys)
        jiraClient.set(testCase.issueKeysIssueIds)

        runTest {
            repository.upsertAll(
                testCase.worklogs.map {
                    it.copy(
                        timeEntry =
                            it.timeEntry.copy(
                                duration = Duration.ofSeconds(0),
                                description = Description(""),
                            )
                    )
                }
            )

            assertEquals(
                testCase.size.toLong(),
                (service.importInRange(testCase.startInclusive, testCase.endInclusive)
                        as WorklogResult.ImportInRangeSuccess)
                    .rowsAffected,
            )

            val result = repository.getAll()

            assertSameElements(testCase.issueKeys, result.map { it.issueKey })
            assertSameElements(testCase.timeEntries, result.map { it.timeEntry })
        }
    }

    @Test
    fun exportByIdsSuccess() {
        val testCase = TestData.testCase()

        runTest {
            repository.upsertAll(testCase.worklogs)

            assertEquals(
                testCase.size.toLong(),
                (service.exportByIds(testCase.ids) as WorklogResult.ExportByIdsSuccess).rowsAffected,
            )
            assertSameElements(testCase.worklogs, tempoClient.get())
            assertSameElements(
                testCase.worklogs.map { it.copy(isExported = true) },
                repository.getAll(),
            )
        }
    }

    @Test
    fun exportByIdsIdsNotFoundError() {
        val testCase = TestData.testCase()
        val (found, notFound) = testCase.partition()

        runTest {
            repository.upsertAll(found.worklogs)

            assertSameElements(
                notFound.ids,
                (service.exportByIds(testCase.ids) as WorklogResult.IdsNotFoundError).ids,
            )
        }
    }

    @Test
    fun deleteByIdsSuccess() {
        val testCase = TestData.testCase()

        runTest {
            repository.upsertAll(testCase.worklogs)

            assertEquals(
                testCase.size.toLong(),
                (service.deleteByIds(testCase.ids) as WorklogResult.DeleteByIdsSuccess).rowsAffected,
            )
            assertSameElements(emptyList(), repository.getAll())
        }
    }

    @Test
    fun deleteByIdsIdsNotFoundError() {
        val testCase = TestData.testCase()
        val (found, notFound) = testCase.partition()

        runTest {
            repository.upsertAll(found.worklogs)

            assertSameElements(
                notFound.ids,
                (service.deleteByIds(testCase.ids) as WorklogResult.IdsNotFoundError).ids,
            )
        }
    }

    @Test
    fun flushSuccess() {
        runTest {
            repository.upsertAll(randomWorklogs())

            assertEquals(WorklogResult.Success, service.flush())
            assertTrue(repository.getAll().isEmpty())
        }
    }

    @BeforeEach
    fun setUp() {
        runBlocking {
            awaitAll(
                async { jiraClient.reset() },
                async { tempoClient.reset() },
                async { togglTrackClient.reset() },
                async { repository.truncate() },
            )
        }
    }

    companion object {
        val jiraClient = JiraClientMock()
        val tempoClient = TempoClientMock()
        val togglTrackClient = TogglTrackClientMock()
        val repository = WorklogRepositoryMock()
        val config = WorklogServiceImplConfig(delimiter = ":")

        val service =
            WorklogServiceImpl(
                jiraClient = jiraClient,
                repository = repository,
                tempoClient = tempoClient,
                togglTrackClient = togglTrackClient,
                config = config,
            )
    }
}
