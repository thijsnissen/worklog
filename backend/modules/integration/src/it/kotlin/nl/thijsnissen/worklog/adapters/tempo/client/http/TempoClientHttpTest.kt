package nl.thijsnissen.worklog.adapters.tempo.client.http

import kotlinx.coroutines.test.runTest
import nl.thijsnissen.worklog.HttpClientLive
import nl.thijsnissen.worklog.JsonMapperBuilderCustomizerLive
import nl.thijsnissen.worklog.MockWebServerBean
import nl.thijsnissen.worklog.TempoClientHttpLive
import nl.thijsnissen.worklog.TestData
import nl.thijsnissen.worklog.adapters.tempo.client.http.dto.Issue
import nl.thijsnissen.worklog.adapters.tempo.client.http.dto.Response
import nl.thijsnissen.worklog.assertSameElements
import nl.thijsnissen.worklog.dispatcher
import nl.thijsnissen.worklog.domain.IssueId
import nl.thijsnissen.worklog.invoke
import nl.thijsnissen.worklog.plus
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor
import tools.jackson.databind.json.JsonMapper

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ContextConfiguration(initializers = [TempoClientHttpTest.Companion.Beans::class])
class TempoClientHttpTest(
    val client: TempoClientHttp,
    val config: TempoClientHttpConfig,
    val server: MockWebServer,
    val jsonMapper: JsonMapper,
) {
    @Test
    fun send() {
        val testCase = TestData.testCase()

        server.withDispatcher(
            issueIds = testCase.issueIds,
            apiKey = config.apiKey,
            jsonMapper = jsonMapper,
        )

        runTest {
            client.send(testCase.worklogs).let {
                assertSameElements(testCase.ids, it)
                assertEquals(testCase.size, it.size)
            }
        }
    }

    companion object {
        fun MockWebServer.withDispatcher(
            issueIds: List<IssueId>,
            apiKey: String,
            jsonMapper: JsonMapper,
        ) =
            this.dispatcher {
                val issueId =
                    IssueId(
                        Regex("/worklogs/issue/([^/]+)/bulk")
                            .find(it.path.orEmpty())
                            ?.groupValues
                            ?.getOrNull(1)
                            ?.toLong() ?: -1
                    )

                val isAuthorized = it.getHeader("Authorization") == "Bearer $apiKey"

                when (it.method) {
                    "POST" if isAuthorized && issueId in issueIds ->
                        MockResponse()
                            .setHeader("Content-Type", "application/json")
                            .setBody(
                                jsonMapper.writeValueAsString(
                                    List(issueIds.count { it == issueId }) {
                                        Response(Issue(id = issueId.value))
                                    }
                                )
                            )
                            .setResponseCode(200)
                    else -> MockResponse().setResponseCode(404)
                }
            }

        object Beans : ApplicationContextInitializer<GenericApplicationContext> {
            override fun initialize(context: GenericApplicationContext) {
                val mockWebServer = MockWebServer()

                System.setProperty(
                    "TEMPO_CLIENT_HTTP_CONFIG_HOST",
                    mockWebServer.url("/").toString(),
                )

                (TempoClientHttpLive +
                        HttpClientLive +
                        MockWebServerBean(mockWebServer) +
                        JsonMapperBuilderCustomizerLive)()
                    .initialize(context)
            }
        }
    }
}
