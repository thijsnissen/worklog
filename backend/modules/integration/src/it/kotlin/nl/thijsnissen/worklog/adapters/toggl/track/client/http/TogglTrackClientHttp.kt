package nl.thijsnissen.worklog.adapters.toggl.track.client.http

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import nl.thijsnissen.worklog.HttpClientLive
import nl.thijsnissen.worklog.MockWebServerBean
import nl.thijsnissen.worklog.TestData
import nl.thijsnissen.worklog.TogglTrackClientHttpConfigLive
import nl.thijsnissen.worklog.TogglTrackClientHttpLive
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.TogglTrackClientHttp.Companion.toRFC3339
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.dto.Response
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.dto.Response.Companion.toOffsetDateTime
import nl.thijsnissen.worklog.assertSameElements
import nl.thijsnissen.worklog.dispatcher
import nl.thijsnissen.worklog.invoke
import nl.thijsnissen.worklog.plus
import okhttp3.Credentials
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.test.context.ContextConfiguration
import tools.jackson.databind.json.JsonMapper

@SpringBootTest
@ContextConfiguration(initializers = [TogglTrackClientHttpTest.Companion.Beans::class])
class TogglTrackClientHttpTest(
    val client: TogglTrackClientHttp,
    val config: TogglTrackClientHttpConfig,
    val server: MockWebServer,
    val jsonMapper: JsonMapper,
) {
    @Test
    fun getTimeEntriesInRange() {
        val testCase = TestData.testCase()

        val response =
            testCase.timeEntriesWithIssueKeys.map {
                Response(
                    it.startInclusive.toOffsetDateTime(from = config.timeZone, to = ZoneOffset.UTC),
                    it.endInclusive.toOffsetDateTime(from = config.timeZone, to = ZoneOffset.UTC),
                    it.duration.seconds,
                    it.description.value,
                )
            }

        server.withDispatcher(
            startInclusive = testCase.startInclusive,
            endInclusive = testCase.endInclusive,
            timeZone = config.timeZone,
            apiToken = config.apiToken,
            response = jsonMapper.writeValueAsString(response),
        )

        runTest {
            assertSameElements(
                testCase.timeEntriesWithIssueKeys,
                client.getTimeEntriesInRange(testCase.startInclusive, testCase.endInclusive),
            )
        }
    }

    companion object {
        fun MockWebServer.withDispatcher(
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
            timeZone: ZoneId,
            apiToken: String,
            response: String,
        ) =
            this.dispatcher {
                val isAuthorized =
                    it.getHeader("Authorization") == Credentials.basic(apiToken, "api_token")

                val path =
                    "/me/time_entries?start_date=${startInclusive.toRFC3339(timeZone)}&end_date=${
                        endInclusive.toRFC3339(
                            timeZone
                        )
                    }"

                when (it.method) {
                    "GET" if isAuthorized && it.path == path ->
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
                    "TOGGL_TRACK_CLIENT_HTTP_CONFIG_HOST",
                    mockWebServer.url("/").toString(),
                )

                (TogglTrackClientHttpLive +
                        TogglTrackClientHttpConfigLive +
                        HttpClientLive +
                        MockWebServerBean(mockWebServer))()
                    .initialize(context)
            }
        }
    }
}
