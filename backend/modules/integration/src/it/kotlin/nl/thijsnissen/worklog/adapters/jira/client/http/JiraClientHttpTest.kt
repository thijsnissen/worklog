package nl.thijsnissen.worklog.adapters.jira.client.http

import kotlinx.coroutines.test.runTest
import nl.thijsnissen.worklog.HttpClientLive
import nl.thijsnissen.worklog.JiraClientHttpLive
import nl.thijsnissen.worklog.JsonMapperBuilderCustomizerLive
import nl.thijsnissen.worklog.MockWebServerBean
import nl.thijsnissen.worklog.TestData
import nl.thijsnissen.worklog.adapters.jira.client.http.dto.BulkFetchResponse
import nl.thijsnissen.worklog.adapters.jira.client.http.dto.Response
import nl.thijsnissen.worklog.dispatcher
import nl.thijsnissen.worklog.invoke
import nl.thijsnissen.worklog.plus
import okhttp3.Credentials
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
@ContextConfiguration(initializers = [JiraClientHttpTest.Companion.Beans::class])
class JiraClientHttpTest(
    val client: JiraClientHttp,
    val config: JiraClientHttpConfig,
    val server: MockWebServer,
    val jsonMapper: JsonMapper,
) {
    @Test
    fun getIssueIds() {
        val testCase = TestData.testCase()

        server.withDispatcher(
            userEmail = config.userEmail,
            apiKey = config.apiKey,
            response =
                jsonMapper.writeValueAsString(
                    BulkFetchResponse(
                        testCase.issueKeysIssueIds.map { (key, id) ->
                            Response(key.value, id.value)
                        }
                    )
                ),
        )

        runTest { assertEquals(testCase.issueKeysIssueIds, client.getIssueIds(testCase.issueKeys)) }
    }

    companion object {
        fun MockWebServer.withDispatcher(userEmail: String, apiKey: String, response: String) =
            this.dispatcher {
                val isAuthorized =
                    it.getHeader("Authorization") == Credentials.basic(userEmail, apiKey)

                when (it.method) {
                    "POST" if isAuthorized && it.path == "/issue/bulkfetch" ->
                        MockResponse()
                            .setHeader("Content-Type", "application/json")
                            .setBody(response)
                            .setResponseCode(200)
                    else -> MockResponse().setResponseCode(404)
                }
            }

        object Beans : ApplicationContextInitializer<GenericApplicationContext> {
            override fun initialize(context: GenericApplicationContext) {
                val mockWebServer = MockWebServer()

                System.setProperty(
                    "JIRA_CLIENT_HTTP_CONFIG_HOST",
                    mockWebServer.url("/").toString(),
                )

                (JiraClientHttpLive +
                        HttpClientLive +
                        MockWebServerBean(mockWebServer) +
                        JsonMapperBuilderCustomizerLive)()
                    .initialize(context)
            }
        }
    }
}
