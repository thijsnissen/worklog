package nl.thijsnissen.worklog.adapters.worklog.repository.h2

import kotlinx.coroutines.test.runTest
import nl.thijsnissen.worklog.TestData
import nl.thijsnissen.worklog.WorklogRepositoryH2Live
import nl.thijsnissen.worklog.assertContainsElements
import nl.thijsnissen.worklog.assertSameElements
import nl.thijsnissen.worklog.invoke
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ContextConfiguration(initializers = [WorklogRepositoryH2Test.Companion.Beans::class])
class WorklogRepositoryH2Test(val repository: WorklogRepositoryH2) {
    @Test
    fun storeAndRetrieve() {
        val createTestCase = TestData.testCase()
        val (updateTestCase, deleteTestCase) =
            TestData.testCase(
                    size = createTestCase.size,
                    ids = createTestCase.ids,
                    isExported = createTestCase.isExported,
                    createdAt = createTestCase.createdAt,
                )
                .partition()
        val isExportedTestCase =
            updateTestCase.copy(
                worklogs = updateTestCase.worklogs.map { it.copy(isExported = true) }
            )

        runTest {
            assertSameElements(emptyList(), repository.getAll())

            assertEquals(
                createTestCase.size.toLong(),
                repository.upsertAll(createTestCase.worklogs),
            )
            assertSameElements(createTestCase.worklogs, repository.getAll())
            assertSameElements(createTestCase.worklogs, repository.getByIds(createTestCase.ids))
            assertSameElements(
                createTestCase.worklogs,
                repository.getByHashes(createTestCase.hashes),
            )

            assertEquals(
                updateTestCase.size.toLong(),
                repository.upsertAll(updateTestCase.worklogs),
            )
            assertContainsElements(updateTestCase.worklogs, repository.getAll())
            assertSameElements(updateTestCase.worklogs, repository.getByIds(updateTestCase.ids))
            assertSameElements(
                updateTestCase.worklogs,
                repository.getByHashes(updateTestCase.hashes),
            )

            assertEquals(deleteTestCase.size.toLong(), repository.deleteByIds(deleteTestCase.ids))
            assertSameElements(updateTestCase.worklogs, repository.getAll())

            assertEquals(
                isExportedTestCase.size.toLong(),
                repository.setIsExportedByIds(isExportedTestCase.ids),
            )
            assertContainsElements(isExportedTestCase.worklogs, repository.getAll())
            assertSameElements(
                isExportedTestCase.worklogs,
                repository.getByIds(isExportedTestCase.ids),
            )
            assertSameElements(
                isExportedTestCase.worklogs,
                repository.getByHashes(isExportedTestCase.hashes),
            )

            repository.truncate()

            assertSameElements(emptyList(), repository.getAll())
        }
    }

    companion object {
        object Beans : ApplicationContextInitializer<GenericApplicationContext> {
            override fun initialize(context: GenericApplicationContext) =
                WorklogRepositoryH2Live().initialize(context)
        }
    }
}
