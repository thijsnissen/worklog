package nl.thijsnissen.worklog.adapters.api.http.worklog

import nl.thijsnissen.http.server.HttpError
import nl.thijsnissen.worklog.ApiHttpWorklogLive
import nl.thijsnissen.worklog.HttpServerCorsWebFilterLive
import nl.thijsnissen.worklog.HttpServerWebExceptionHandlerLive
import nl.thijsnissen.worklog.TestData.Companion.randomIssueKeys
import nl.thijsnissen.worklog.TestData.Companion.randomLocalDateTime
import nl.thijsnissen.worklog.TestData.Companion.randomLong
import nl.thijsnissen.worklog.TestData.Companion.randomString
import nl.thijsnissen.worklog.TestData.Companion.randomUuids
import nl.thijsnissen.worklog.TestData.Companion.randomWorklogs
import nl.thijsnissen.worklog.WebTestClientBean
import nl.thijsnissen.worklog.WorklogServiceMockBean
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.DeleteByIdsRequest
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.DeleteByIdsResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ExportByIdsRequest
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ExportByIdsResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.GetAllResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ImportInRangeResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.Worklog as WorklogDto
import nl.thijsnissen.worklog.assertSameElements
import nl.thijsnissen.worklog.domain.WorklogResult
import nl.thijsnissen.worklog.invoke
import nl.thijsnissen.worklog.plus
import nl.thijsnissen.worklog.ports.incoming.WorklogServiceMock
import nl.thijsnissen.worklog.urlEncode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ContextConfiguration(initializers = [ApiHttpWorklogTest.Companion.Beans::class])
class ApiHttpWorklogTest(val client: WebTestClient, val service: WorklogServiceMock) {
    @Test
    fun getWorklogsGetAllSuccess() {
        val result = WorklogResult.WorklogsSuccess(randomWorklogs())
        val response = GetAllResponse(result.worklogs.map(WorklogDto::fromDomain))

        service.set(result)

        client
            .get()
            .uri("/api/worklogs")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<GetAllResponse>()
            .isEqualTo(response)
    }

    @Test
    fun getWorklogsGetAllError() {
        service.error(RuntimeException())

        client
            .get()
            .uri("/api/worklogs")
            .exchange()
            .expectStatus()
            .is5xxServerError
            .expectBody<HttpError>()
            .value {
                assertEquals(500, it?.status)
                assertEquals("Internal Server Error", it?.code)
            }
    }

    @Test
    fun postWorklogsImportInRangeSuccess() {
        val affectedRows = randomLong(min = 1)
        val dateTime = randomLocalDateTime().urlEncode()
        val response = ImportInRangeResponse(affectedRows)

        service.set(WorklogResult.ImportInRangeSuccess(affectedRows))

        client
            .post()
            .uri("/api/worklogs?startInclusive=${dateTime}&endInclusive=${dateTime}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<ImportInRangeResponse>()
            .isEqualTo(response)
    }

    @Test
    fun postWorklogsImportInRangeNoTimeEntriesFoundError() {
        val dateTime = randomLocalDateTime().urlEncode()
        val response = ImportInRangeResponse(0)

        service.set(WorklogResult.NoTimeEntriesFoundError)

        client
            .post()
            .uri("/api/worklogs?startInclusive=${dateTime}&endInclusive=${dateTime}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<ImportInRangeResponse>()
            .isEqualTo(response)
    }

    @Test
    fun postWorklogsImportInRangeInvalidDateTimeRangeError() {
        val dateTime = randomLocalDateTime().urlEncode()
        val details = randomString()

        service.set(WorklogResult.InvalidDateTimeRangeError(details))

        client
            .post()
            .uri("/api/worklogs?startInclusive=${dateTime}&endInclusive=${dateTime}")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody<HttpError>()
            .value {
                assertEquals(400, it?.status)
                assertEquals("Bad Request", it?.code)
                assertEquals(details, it?.message)
            }
    }

    @Test
    fun postWorklogsImportInRangeInvalidDateTimeFormatError() {
        val dateTime = "invalid"

        client
            .post()
            .uri("/api/worklogs?startInclusive=${dateTime}&endInclusive=${dateTime}")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody<HttpError>()
            .value {
                assertEquals(400, it?.status)
                assertEquals("Bad Request", it?.code)
            }
    }

    @Test
    fun postWorklogsImportInRangeNoDateTimeRangeError() {
        client
            .post()
            .uri("/api/worklogs")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody<HttpError>()
            .value {
                assertEquals(400, it?.status)
                assertEquals("Bad Request", it?.code)
            }
    }

    @Test
    fun postWorklogsImportInRangeIssueKeysNotFoundError() {
        val dateTime = randomLocalDateTime().urlEncode()
        val issueKeys = randomIssueKeys()
        val errors = issueKeys.map { HttpError.Companion.ValidationError("issueKey", it.value) }

        service.set(WorklogResult.IssueKeysNotFoundError(issueKeys))

        client
            .post()
            .uri("/api/worklogs?startInclusive=${dateTime}&endInclusive=${dateTime}")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody<HttpError>()
            .value {
                assertEquals(400, it?.status)
                assertEquals("Bad Request", it?.code)
                assertSameElements(errors, it?.errors.orEmpty())
            }
    }

    @Test
    fun postWorklogsImportInRangeError() {
        val dateTime = randomLocalDateTime().urlEncode()

        service.error(RuntimeException())

        client
            .post()
            .uri("/api/worklogs?startInclusive=${dateTime}&endInclusive=${dateTime}")
            .exchange()
            .expectStatus()
            .is5xxServerError
            .expectBody<HttpError>()
            .value {
                assertEquals(500, it?.status)
                assertEquals("Internal Server Error", it?.code)
            }
    }

    @Test
    fun postWorklogsExportExportByIdsSuccess() {
        val request = ExportByIdsRequest(randomUuids())

        service.set(WorklogResult.ExportByIdsSuccess(request.ids.size.toLong()))

        client
            .post()
            .uri("/api/worklogs/export")
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<ExportByIdsResponse>()
            .value { assertEquals(request.ids.size.toLong(), it?.rowsAffected) }
    }

    @Test
    fun postWorklogsExportExportByIdsValidationError() {
        val request = ExportByIdsRequest(ids = emptyList())
        val errors =
            listOf(
                HttpError.Companion.ValidationError(field = "ids", message = "ids cannot be empty")
            )

        client
            .post()
            .uri("/api/worklogs/export")
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody<HttpError>()
            .value {
                assertEquals(400, it?.status)
                assertEquals("Bad Request", it?.code)
                assertEquals("Validation Failed", it?.message)
                assertSameElements(errors, it?.errors.orEmpty())
            }
    }

    @Test
    fun postWorklogsExportExportByIdsIdsNotFoundError() {
        val ids = randomUuids()
        val request = ExportByIdsRequest(ids)
        val errors =
            ids.map { HttpError.Companion.ValidationError(field = "id", message = it.toString()) }

        service.set(WorklogResult.IdsNotFoundError(ids))

        client
            .post()
            .uri("/api/worklogs/export")
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody<HttpError>()
            .value {
                assertEquals(400, it?.status)
                assertEquals("Bad Request", it?.code)
                assertEquals(errors, it?.errors.orEmpty())
            }
    }

    @Test
    fun postWorklogsExportExportByIdsError() {
        service.error(RuntimeException())

        client
            .post()
            .uri("/api/worklogs/export")
            .bodyValue(ExportByIdsRequest(randomUuids()))
            .exchange()
            .expectStatus()
            .is5xxServerError
            .expectBody<HttpError>()
            .value {
                assertEquals(500, it?.status)
                assertEquals("Internal Server Error", it?.code)
            }
    }

    @Test
    fun postWorklogsDeleteDeleteByIdsSuccess() {
        val request = DeleteByIdsRequest(randomUuids())

        service.set(WorklogResult.DeleteByIdsSuccess(request.ids.size.toLong()))

        client
            .post()
            .uri("/api/worklogs/delete")
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<DeleteByIdsResponse>()
            .value { assertEquals(request.ids.size.toLong(), it?.rowsAffected) }
    }

    @Test
    fun postWorklogsDeleteDeleteByIdsValidationError() {
        val request = DeleteByIdsRequest(ids = emptyList())
        val errors =
            listOf(
                HttpError.Companion.ValidationError(field = "ids", message = "ids cannot be empty")
            )

        client
            .post()
            .uri("/api/worklogs/delete")
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody<HttpError>()
            .value {
                assertEquals(400, it?.status)
                assertEquals("Bad Request", it?.code)
                assertEquals("Validation Failed", it?.message)
                assertSameElements(errors, it?.errors.orEmpty())
            }
    }

    @Test
    fun postWorklogsDeleteDeleteByIdsIdsNotFoundError() {
        val ids = randomUuids()
        val request = DeleteByIdsRequest(ids)
        val errors =
            ids.map { HttpError.Companion.ValidationError(field = "id", message = it.toString()) }

        service.set(WorklogResult.IdsNotFoundError(ids))

        client
            .post()
            .uri("/api/worklogs/delete")
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody<HttpError>()
            .value {
                assertEquals(400, it?.status)
                assertEquals("Bad Request", it?.code)
                assertEquals(errors, it?.errors.orEmpty())
            }
    }

    @Test
    fun postWorklogsDeleteDeleteByIdsError() {
        service.error(RuntimeException())

        client
            .post()
            .uri("/api/worklogs/delete")
            .bodyValue(DeleteByIdsRequest(randomUuids()))
            .exchange()
            .expectStatus()
            .is5xxServerError
            .expectBody<HttpError>()
            .value {
                assertEquals(500, it?.status)
                assertEquals("Internal Server Error", it?.code)
            }
    }

    @Test
    fun deleteWorklogsFlushSuccess() {
        service.set(WorklogResult.Success)

        client.delete().uri("/api/worklogs").exchange().expectStatus().isNoContent
    }

    @Test
    fun deleteWorklogsFlushError() {
        service.error(RuntimeException())

        client
            .delete()
            .uri("/api/worklogs")
            .exchange()
            .expectStatus()
            .is5xxServerError
            .expectBody<HttpError>()
            .value {
                assertEquals(500, it?.status)
                assertEquals("Internal Server Error", it?.code)
            }
    }

    @BeforeEach
    fun setUp() {
        service.reset()
    }

    companion object {
        object Beans : ApplicationContextInitializer<GenericApplicationContext> {
            override fun initialize(context: GenericApplicationContext) =
                (ApiHttpWorklogLive +
                        HttpServerWebExceptionHandlerLive +
                        HttpServerCorsWebFilterLive +
                        WebTestClientBean(context) +
                        WorklogServiceMockBean)()
                    .initialize(context)
        }
    }
}
