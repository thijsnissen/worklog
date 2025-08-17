package nl.thijsnissen.worklog.adapters.tempo.client.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.format.DateTimeFormatter
import nl.thijsnissen.worklog.TestData.Companion.randomInt
import nl.thijsnissen.worklog.TestData.Companion.randomIssueIds
import nl.thijsnissen.worklog.TestData.Companion.randomLocalDateTime
import nl.thijsnissen.worklog.TestData.Companion.randomLong
import nl.thijsnissen.worklog.TestData.Companion.randomString
import nl.thijsnissen.worklog.TestData.Companion.randomTimeEntries
import nl.thijsnissen.worklog.adapters.tempo.client.http.dto.BulkResponse
import nl.thijsnissen.worklog.adapters.tempo.client.http.dto.Request.Companion.fromDomain
import nl.thijsnissen.worklog.assertSameElements
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.TestConstructor

@JsonTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class JsonParsingTest(val objectMapper: ObjectMapper) {
    @Test
    fun encodeBulkRequest() {
        val authorAccountId = randomString()
        val request = randomTimeEntries().fromDomain(authorAccountId)

        val json =
            request.joinToString {
                """
                    {
                        "authorAccountId": "$authorAccountId",
                        "description": "${it.description}",
                        "startDate": "${it.startDate.format(DateTimeFormatter.ofPattern("uuuu-MM-dd"))}",
                        "startTime": "${it.startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}",
                        "timeSpentSeconds": ${it.timeSpentSeconds}
                    }
                 """
                    .trimIndent()
            }

        JSONAssert.assertEquals(
            "[ $json ]",
            objectMapper.writeValueAsString(request),
            JSONCompareMode.LENIENT,
        )
    }

    @Test
    fun decodeBulkResponse() {
        val issueIds = randomIssueIds()

        val response =
            issueIds.joinToString {
                val accountId = randomString()
                val startDateTime = randomLocalDateTime()
                val tempoWorklogId = randomInt()

                """
                {
                    "self": "https://api.eu.tempo.io/4/worklogs/$tempoWorklogId",
                    "tempoWorklogId": $tempoWorklogId,
                    "issue":
                    {
                        "self": "https://dhlbnl.atlassian.net/rest/api/2/issue/${it.value}",
                        "id": ${it.value}
                    },
                    "timeSpentSeconds": ${randomLong()},
                    "billableSeconds": 0,
                    "startDate": "${startDateTime.toLocalDate()}",
                    "startTime": "${startDateTime.toLocalTime()}",
                    "startDateTimeUtc": "$startDateTime",
                    "description": "${randomString()}",
                    "createdAt": "${randomLocalDateTime()}",
                    "updatedAt": "${randomLocalDateTime()}",
                    "author":
                    {
                        "self": "https://dhlbnl.atlassian.net/rest/api/2/user?accountId=$accountId",
                        "accountId": "$accountId"
                    },
                    "attributes":
                    {
                        "self": "https://api.eu.tempo.io/4/worklogs/$tempoWorklogId/work-attribute-values",
                        "values":
                        []
                    }
                }
            """
                    .trimIndent()
            }

        objectMapper.readValue<BulkResponse>("[ $response ]").let {
            assertSameElements(issueIds.map { it.value }, it.map { it.issue.id })
            assertEquals(issueIds.size, it.size)
        }
    }
}
