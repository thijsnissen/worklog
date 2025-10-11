package nl.thijsnissen.worklog

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.test.runTest
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.DeleteByIdsRequest
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.DeleteByIdsResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ExportByIdsRequest
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ExportByIdsResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.GetAllResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ImportInRangeResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.Worklog
import nl.thijsnissen.worklog.adapters.jira.client.http.JiraClientHttpConfig
import nl.thijsnissen.worklog.adapters.jira.client.http.dto.BulkFetchResponse
import nl.thijsnissen.worklog.adapters.jira.client.http.dto.Response as JiraClientResponse
import nl.thijsnissen.worklog.adapters.tempo.client.http.TempoClientHttpConfig
import nl.thijsnissen.worklog.adapters.tempo.client.http.dto.BulkRequest
import nl.thijsnissen.worklog.adapters.tempo.client.http.dto.Issue
import nl.thijsnissen.worklog.adapters.tempo.client.http.dto.Response as TempoClientResponse
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.TogglTrackClientHttp.Companion.toRFC3339
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.TogglTrackClientHttpConfig
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.dto.InRangeResponse
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.dto.Response as TogglTrackClientResponse
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.dto.Response.Companion.toOffsetDateTime
import nl.thijsnissen.worklog.core.WorklogServiceImplConfig
import nl.thijsnissen.worklog.domain.IssueId
import okhttp3.Credentials
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ContextConfiguration(initializers = [ApplicationTest.Companion.Beans::class])
class ApplicationTest(
    val client: WebClient,
    val server: MockWebServer,
    val jsonMapper: JsonMapper,
    val jiraClientHttpConfig: JiraClientHttpConfig,
    val tempoClientHttpConfig: TempoClientHttpConfig,
    val togglTrackClientHttpConfig: TogglTrackClientHttpConfig,
    val worklogServiceImplConfig: WorklogServiceImplConfig,
) {
    @Test
    fun worklogHappyFlow() {
        val createTestCase = TestData.testCase(delimiter = worklogServiceImplConfig.delimiter)
        val deleteTestCase = TestData.testCase(delimiter = worklogServiceImplConfig.delimiter)

        runTest {
            setupTestData(createTestCase)

            assertSameElements(emptyList(), client.getWorklogsGetAllRequest().worklogs)

            assertEquals(
                createTestCase.size.toLong(),
                client
                    .postWorklogsImportInRangeRequest(
                        createTestCase.startInclusive,
                        createTestCase.endInclusive,
                    )
                    .rowsAffected,
            )

            val createWorklogs = client.getWorklogsGetAllRequest().worklogs

            assertWorklogs(createTestCase, createWorklogs, isExported = false)

            setupTestData(deleteTestCase)

            assertEquals(
                deleteTestCase.size.toLong(),
                client
                    .postWorklogsImportInRangeRequest(
                        deleteTestCase.startInclusive,
                        deleteTestCase.endInclusive,
                    )
                    .rowsAffected,
            )

            val deleteWorklogs = client.getWorklogsGetAllRequest().worklogs - createWorklogs

            assertWorklogs(deleteTestCase, deleteWorklogs, isExported = false)

            assertEquals(
                deleteTestCase.size.toLong(),
                client
                    .postWorklogsDeleteDeleteByIdsRequest(deleteWorklogs.map { it.id })
                    .rowsAffected,
            )
            assertWorklogs(
                createTestCase,
                client.getWorklogsGetAllRequest().worklogs,
                isExported = false,
            )

            setupTestData(createTestCase)

            assertEquals(
                createTestCase.size.toLong(),
                client
                    .postWorklogsExportExportByIdsRequest(createWorklogs.map { it.id })
                    .rowsAffected,
            )

            assertWorklogs(
                createTestCase,
                client.getWorklogsGetAllRequest().worklogs,
                isExported = true,
            )

            assertSameElements(createTestCase.issueIds, tempoClientRequestRef.get().keys)

            client.deleteWorklogsFlushRequest()

            assertSameElements(emptyList(), client.getWorklogsGetAllRequest().worklogs)
        }
    }

    fun setupTestData(testCase: TestData) {
        val jiraClientResponse =
            BulkFetchResponse(
                testCase.issueKeysIssueIds.map { (key, id) ->
                    JiraClientResponse(key.value, id.value)
                }
            )

        val togglTrackResponse =
            testCase.timeEntriesWithIssueKeys.map {
                TogglTrackClientResponse(
                    it.startInclusive.toOffsetDateTime(
                        from = togglTrackClientHttpConfig.timeZone,
                        to = ZoneOffset.UTC,
                    ),
                    it.endInclusive.toOffsetDateTime(
                        from = togglTrackClientHttpConfig.timeZone,
                        to = ZoneOffset.UTC,
                    ),
                    it.duration.seconds,
                    it.description.value,
                )
            }

        server.withDispatcher(
            jsonMapper = jsonMapper,
            togglTrackClientApiToken = togglTrackClientHttpConfig.apiToken,
            startInclusive = testCase.startInclusive,
            endInclusive = testCase.endInclusive,
            timeZone = togglTrackClientHttpConfig.timeZone,
            jiraClientUserEmail = jiraClientHttpConfig.userEmail,
            jiraClientApiKey = jiraClientHttpConfig.apiKey,
            jiraClientResponse = jiraClientResponse,
            togglTrackResponse = togglTrackResponse,
            tempoClientApiKey = tempoClientHttpConfig.apiKey,
            issueIds = testCase.issueIds,
        )
    }

    companion object {
        val tempoClientRequestRef: AtomicReference<Map<IssueId, List<String>>> =
            AtomicReference(emptyMap())

        const val BASE_URL = "http://localhost:8080/api/v1"

        suspend fun WebClient.getWorklogsGetAllRequest(): GetAllResponse =
            this.get().uri("$BASE_URL/worklogs").retrieve().awaitBody<GetAllResponse>()

        suspend fun WebClient.postWorklogsImportInRangeRequest(
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
        ): ImportInRangeResponse =
            this.post()
                .uri(
                    "$BASE_URL/worklogs?startInclusive=${startInclusive.urlEncode()}&endInclusive=${endInclusive.urlEncode()}"
                )
                .retrieve()
                .awaitBody<ImportInRangeResponse>()

        suspend fun WebClient.postWorklogsExportExportByIdsRequest(
            ids: List<UUID>
        ): ExportByIdsResponse =
            this.post()
                .uri("$BASE_URL/worklogs/export")
                .bodyValue(ExportByIdsRequest(ids))
                .retrieve()
                .awaitBody<ExportByIdsResponse>()

        suspend fun WebClient.postWorklogsDeleteDeleteByIdsRequest(
            ids: List<UUID>
        ): DeleteByIdsResponse =
            this.post()
                .uri("$BASE_URL/worklogs/delete")
                .bodyValue(DeleteByIdsRequest(ids))
                .retrieve()
                .awaitBody<DeleteByIdsResponse>()

        suspend fun WebClient.deleteWorklogsFlushRequest() {
            this.delete().uri("$BASE_URL/worklogs").retrieve().awaitBodilessEntity()
        }

        fun MockWebServer.withDispatcher(
            jsonMapper: JsonMapper,
            togglTrackClientApiToken: String,
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
            timeZone: ZoneId,
            togglTrackResponse: InRangeResponse,
            jiraClientUserEmail: String,
            jiraClientApiKey: String,
            jiraClientResponse: BulkFetchResponse,
            tempoClientApiKey: String,
            issueIds: List<IssueId>,
        ) =
            this.dispatcher {
                // Toggl Track Client
                val isAuthorizedTogglTrackClient =
                    it.getHeader("Authorization") ==
                        Credentials.basic(togglTrackClientApiToken, "api_token")

                val pathTogglTrackClient =
                    "/me/time_entries?start_date=${startInclusive.toRFC3339(timeZone)}&end_date=${endInclusive.toRFC3339(timeZone)}"

                val responseTogglTrackClient =
                    MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setBody(jsonMapper.writeValueAsString(togglTrackResponse))
                        .setResponseCode(200)

                // JiraClient
                val isAuthorizedJiraClient =
                    it.getHeader("Authorization") ==
                        Credentials.basic(jiraClientUserEmail, jiraClientApiKey)

                val pathJiraClient = "/issue/bulkfetch"

                val responseJiraClient =
                    MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setBody(jsonMapper.writeValueAsString(jiraClientResponse))
                        .setResponseCode(200)

                // Tempo Client
                val isAuthorizedTempoClient =
                    it.getHeader("Authorization") == "Bearer $tempoClientApiKey"

                val issueIdTempoClient =
                    IssueId(
                        Regex("/worklogs/issue/([^/]+)/bulk")
                            .find(it.path.orEmpty())
                            ?.groupValues
                            ?.getOrNull(1)
                            ?.toLong() ?: -1
                    )

                val responseTempoClient =
                    MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setBody(
                            jsonMapper.writeValueAsString(
                                List(issueIds.count { it == issueIdTempoClient }) {
                                    TempoClientResponse(Issue(id = issueIdTempoClient.value))
                                }
                            )
                        )
                        .setResponseCode(200)

                when (it.method) {
                    "GET" if isAuthorizedTogglTrackClient && it.path == pathTogglTrackClient ->
                        responseTogglTrackClient

                    "POST" if isAuthorizedJiraClient && it.path == pathJiraClient ->
                        responseJiraClient

                    "POST" if isAuthorizedTempoClient && issueIdTempoClient in issueIds -> {
                        val request = jsonMapper.readValue<BulkRequest>(it.body.readUtf8())

                        tempoClientRequestRef
                            .updateAndGet {
                                it + (issueIdTempoClient to request.map { it.description })
                            }
                            .let { responseTempoClient }
                    }

                    else -> MockResponse().setResponseCode(404)
                }
            }

        fun assertWorklogs(expected: TestData, actual: List<Worklog>, isExported: Boolean) {
            assertSameElements(expected.issueKeys.map { it.value }, actual.map { it.issueKey })
            assertSameElements(expected.issueIds.map { it.value }, actual.map { it.issueId })
            assertSameElements(
                expected.timeEntries.map { it.description.value },
                actual.map { it.description },
            )
            assertSameElements(
                expected.timeEntries.map { it.duration.seconds },
                actual.map { it.durationSeconds },
            )
            assertSameElements(
                expected.timeEntries.map { it.startInclusive },
                actual.map { it.startInclusive },
            )
            assertSameElements(
                expected.timeEntries.map { it.endInclusive },
                actual.map { it.endInclusive },
            )
            assertTrue(actual.map { it.exported }.all { it == isExported })
        }

        object Beans : ApplicationContextInitializer<GenericApplicationContext> {
            override fun initialize(context: GenericApplicationContext) {
                val mockWebServer = MockWebServer()
                val mockWebServerUrl = mockWebServer.url("/").toString()

                System.setProperty("TOGGL_TRACK_CLIENT_HTTP_CONFIG_HOST", mockWebServerUrl)
                System.setProperty("JIRA_CLIENT_HTTP_CONFIG_HOST", mockWebServerUrl)
                System.setProperty("TEMPO_CLIENT_HTTP_CONFIG_HOST", mockWebServerUrl)

                (
                    // Service
                    WorklogServiceImplLive +

                        // Adapters
                        ApiHttpWorklogLive +
                        JiraClientHttpLive +
                        TempoClientHttpLive +
                        TogglTrackClientHttpConfigLive +
                        TogglTrackClientHttpLive +
                        WorklogRepositoryH2Live +

                        // Libs
                        HttpClientLive +
                        HttpServerWebExceptionHandlerLive +
                        HttpServerCorsWebFilterLive +

                        // Test
                        MockWebServerBean(mockWebServer))()
                    .initialize(context)
            }
        }
    }
}
