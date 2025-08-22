package nl.thijsnissen.worklog.adapters.api.http.worklog

import nl.thijsnissen.http.openapi.OpenApi.OpenApiItem
import nl.thijsnissen.http.openapi.OpenApi.OpenApiOperation.Companion.operation
import nl.thijsnissen.http.openapi.OpenApi.OpenApiParameter.Companion.queryParameter
import nl.thijsnissen.http.openapi.OpenApi.OpenApiPathItem.Companion.HttpMethod
import nl.thijsnissen.http.openapi.OpenApi.OpenApiPathItem.Companion.pathItem
import nl.thijsnissen.http.openapi.OpenApi.OpenApiResponse
import nl.thijsnissen.http.openapi.OpenApi.OpenApiResponse.Companion.response
import nl.thijsnissen.http.openapi.OpenApi.OpenApiServer
import nl.thijsnissen.http.server.HttpError
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.DeleteByIdsRequest
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.DeleteByIdsResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ExportByIdsRequest
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ExportByIdsResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.GetAllResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ImportInRangeResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter

class Endpoints(val handler: Handler) {
    fun endpoints() = coRouter {
        "api/v1"
            .nest {
                GET("/worklogs", handler::getAll)

                POST("/worklogs", handler::importInRange)

                POST("/worklogs/delete", handler::deleteByIds)

                DELETE("/worklogs", handler::flush)

                accept(APPLICATION_JSON).nest { POST("/worklogs/export", handler::exportByIds) }
            }
    }

    companion object {
        const val TAG = "Worklogs"

        val errorResponses: List<OpenApiResponse> =
            listOf(
                response<HttpError>(HttpStatus.NOT_FOUND, "Not Found"),
                response<HttpError>(HttpStatus.BAD_REQUEST, "Bad Request"),
                response<HttpError>(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
            )

        fun spec(): List<OpenApiItem> =
            listOf(
                OpenApiItem(
                    "/worklogs",
                    pathItem(
                        HttpMethod.GET to
                            operation<Any>(
                                operationId = "getAll",
                                summary = "Get all worklogs",
                                tag = TAG,
                                responses =
                                    listOf(response<GetAllResponse>(HttpStatus.OK)) + errorResponses,
                            ),
                        HttpMethod.POST to
                            operation<Any>(
                                operationId = "importInRange",
                                summary =
                                    "Import all worklogs in the range between startInclusive and endInclusive",
                                tag = TAG,
                                responses =
                                    listOf(response<ImportInRangeResponse>(HttpStatus.OK)) +
                                        errorResponses,
                                parameters =
                                    listOf(
                                        queryParameter(
                                            "startInclusive",
                                            "Start ISO local date-time without offset of the range to import",
                                            required = true,
                                        ),
                                        queryParameter(
                                            "endInclusive",
                                            "End ISO local date-time without offset of the range to import",
                                            required = true,
                                        ),
                                    ),
                            ),
                        HttpMethod.DELETE to
                            operation<Any>(
                                operationId = "flush",
                                summary = "Flush all worklogs",
                                tag = TAG,
                                responses =
                                    listOf(response<Any>(HttpStatus.NO_CONTENT)) + errorResponses,
                            ),
                    ),
                ),
                OpenApiItem(
                    "/worklogs/export",
                    pathItem(
                        HttpMethod.POST to
                            operation<ExportByIdsRequest>(
                                operationId = "export",
                                summary = "Export worklogs by uuid",
                                tag = TAG,
                                responses =
                                    listOf(response<ExportByIdsResponse>(HttpStatus.OK)) +
                                        errorResponses,
                            )
                    ),
                ),
                OpenApiItem(
                    "/worklogs/delete",
                    pathItem(
                        HttpMethod.POST to
                            operation<DeleteByIdsRequest>(
                                operationId = "deleteByIds",
                                summary = "Delete worklogs by uuid",
                                tag = TAG,
                                responses =
                                    listOf(response<DeleteByIdsResponse>(HttpStatus.OK)) +
                                        errorResponses,
                            )
                    ),
                ),
            )

        fun servers(): List<OpenApiServer> =
            listOf(
                OpenApiServer(url = "http://localhost:8080/api/v1", description = "Worklog server")
            )
    }
}
