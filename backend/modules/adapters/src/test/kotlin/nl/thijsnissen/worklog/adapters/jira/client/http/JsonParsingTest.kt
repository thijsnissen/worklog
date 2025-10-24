package nl.thijsnissen.worklog.adapters.jira.client.http

import nl.thijsnissen.worklog.TestData.Companion.randomLongs
import nl.thijsnissen.worklog.TestData.Companion.randomStrings
import nl.thijsnissen.worklog.adapters.jira.client.http.dto.BulkFetchRequest
import nl.thijsnissen.worklog.adapters.jira.client.http.dto.BulkFetchResponse
import nl.thijsnissen.worklog.assertSameElements
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.TestConstructor
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

@JsonTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class JsonParsingTest(val jsonMapper: JsonMapper) {
    @Test
    fun encodeBulkFetchRequest() {
        val request = BulkFetchRequest(issueIdsOrKeys = randomStrings())

        val json =
            """
                {
                  "issueIdsOrKeys": [
                    ${request.issueIdsOrKeys.joinToString { "\"$it\"" }}
                  ],
                  "fields": [
                    ""
                  ]
                }
            """
                .trimIndent()

        JSONAssert.assertEquals(
            jsonMapper.writeValueAsString(request),
            json,
            JSONCompareMode.LENIENT,
        )
    }

    @Test
    fun decodeBulkFetchResponse() {
        val issueKeys = randomStrings()
        val issueIds = randomLongs(size = issueKeys.size)

        val issues =
            issueKeys.zip(issueIds).joinToString { (key, id) ->
                """
                    {
                      "expand": "operations,versionedRepresentations,editmeta,changelog,renderedFields",
                      "id": "$id",
                      "self": "https://atlassian.net/rest/api/3/issue/bulkfetch/$id",
                      "key": "$key"
                    }
                """
                    .trimIndent()
            }

        val response =
            """
                {
                  "expand": "names,schema",
                  "issues": [
                    $issues
                  ],
                  "issueErrors": []
                }
            """
                .trimIndent()

        jsonMapper.readValue<BulkFetchResponse>(response).let {
            assertSameElements(issueIds, it.issues.map { it.id })
            assertSameElements(issueKeys, it.issues.map { it.key })
            assertEquals(issueKeys.size, it.issues.size)
        }
    }
}
