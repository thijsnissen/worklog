package nl.thijsnissen.worklog.adapters.toggl.track.client.http

import java.time.ZoneOffset
import nl.thijsnissen.worklog.JsonMapperBuilderCustomizerLive
import nl.thijsnissen.worklog.TestData.Companion.randomBoolean
import nl.thijsnissen.worklog.TestData.Companion.randomInt
import nl.thijsnissen.worklog.TestData.Companion.randomLocalDateTime
import nl.thijsnissen.worklog.TestData.Companion.randomTimeEntries
import nl.thijsnissen.worklog.TogglTrackClientHttpConfigLive
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.dto.InRangeResponse
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.dto.Response.Companion.toLocalDateTime
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.dto.Response.Companion.toOffsetDateTime
import nl.thijsnissen.worklog.assertSameElements
import nl.thijsnissen.worklog.invoke
import nl.thijsnissen.worklog.plus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ContextConfiguration(initializers = [JsonParsingTest.Companion.Beans::class])
class JsonParsingTest(val jsonMapper: JsonMapper, val config: TogglTrackClientHttpConfig) {
    @Test
    fun decodeInRangeResponse() {
        val entries = randomTimeEntries()

        val json =
            entries.joinToString {
                """
                {
                    "id": ${randomInt()},
                    "workspace_id": ${randomInt()},
                    "project_id": ${randomInt()},
                    "task_id": null,
                    "billable": ${randomBoolean()},
                    "start": "${it.startInclusive.toOffsetDateTime(from = config.timeZone, to = ZoneOffset.UTC)}",
                    "stop": "${it.endInclusive.toOffsetDateTime(from = config.timeZone, to = ZoneOffset.UTC)}",
                    "duration": ${it.duration.seconds},
                    "description": "${it.description.value}",
                    "tags": [],
                    "tag_ids": [],
                    "duronly": true,
                    "at": "${randomLocalDateTime().toOffsetDateTime(from = config.timeZone, to = ZoneOffset.UTC)}",
                    "server_deleted_at": null,
                    "user_id": ${randomInt()},
                    "uid": ${randomInt()},
                    "wid": ${randomInt()},
                    "pid": ${randomInt()}
                }
            """
                    .trimIndent()
            }

        jsonMapper.readValue<InRangeResponse>("[ $json ]").let {
            assertSameElements(entries.map { it.description.value }, it.map { it.description })
            assertSameElements(
                entries.map { it.startInclusive },
                it.map { it.start.toLocalDateTime(config.timeZone) },
            )
            assertSameElements(
                entries.map { it.endInclusive },
                it.map { it.stop.toLocalDateTime(config.timeZone) },
            )
            assertSameElements(entries.map { it.duration.seconds }, it.map { it.duration })
            assertEquals(entries.size, it.size)
        }
    }

    companion object {
        object Beans : ApplicationContextInitializer<GenericApplicationContext> {
            override fun initialize(context: GenericApplicationContext) {
                (TogglTrackClientHttpConfigLive + JsonMapperBuilderCustomizerLive)()
                    .initialize(context)
            }
        }
    }
}
