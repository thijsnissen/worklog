package nl.thijsnissen.worklog.adapters.api.http.worklog

import jakarta.validation.Validator
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.jvm.optionals.getOrNull
import nl.thijsnissen.http.server.HttpError
import nl.thijsnissen.http.server.awaitValidatedBody
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.DeleteByIdsRequest
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.DeleteByIdsResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ExportByIdsRequest
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ExportByIdsResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.GetAllResponse
import nl.thijsnissen.worklog.adapters.api.http.worklog.dto.ImportInRangeResponse
import nl.thijsnissen.worklog.domain.WorklogResult
import nl.thijsnissen.worklog.ports.incoming.WorklogService
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait

class Handler(val service: WorklogService, val validator: Validator) {
    suspend fun getAll(request: ServerRequest): ServerResponse =
        when (val result = service.getAll()) {
            is WorklogResult.WorklogsSuccess ->
                ServerResponse.ok().bodyValueAndAwait(GetAllResponse.fromDomain(result.worklogs))
            else -> HttpError.internalServerError.toServerResponse()
        }

    suspend fun importInRange(request: ServerRequest): ServerResponse =
        request.queryParam("startInclusive").getOrNull()?.toLocalDateTime()?.let { startInclusive ->
            request.queryParam("endInclusive").getOrNull()?.toLocalDateTime()?.let { endInclusive ->
                when (val result = service.importInRange(startInclusive, endInclusive)) {
                    is ImportInRangeSuccess ->
                        ServerResponse.ok()
                            .bodyValueAndAwait(ImportInRangeResponse(result.rowsAffected))
                    is NoTimeEntriesFoundError ->
                        ServerResponse.ok().bodyValueAndAwait(ImportInRangeResponse(0))
                    is InvalidDateTimeRangeError ->
                        HttpStatus.BAD_REQUEST.let {
                                HttpError(
                                    status = it.value(),
                                    code = it.reasonPhrase,
                                    message = result.message,
                                )
                            }
                            .toServerResponse()
                    is IssueKeysNotFoundError ->
                        HttpStatus.BAD_REQUEST.let {
                                HttpError(
                                    status = it.value(),
                                    code = it.reasonPhrase,
                                    message =
                                        "Import aborted because some entries where not found.",
                                    errors =
                                        result.entries.map {
                                            HttpError.Companion.ValidationError(
                                                field = "issueKey",
                                                message = it.value,
                                            )
                                        },
                                )
                            }
                            .toServerResponse()
                    else -> HttpError.internalServerError.toServerResponse()
                }
            }
        }
            ?: HttpStatus.BAD_REQUEST.let {
                    HttpError(
                        status = it.value(),
                        code = it.reasonPhrase,
                        "Invalid date/time range",
                    )
                }
                .toServerResponse()

    suspend fun exportByIds(request: ServerRequest): ServerResponse =
        context(validator) {
            request.awaitValidatedBody<ExportByIdsRequest>().let {
                when (val result = service.exportByIds(it.ids)) {
                    is ExportByIdsSuccess ->
                        ServerResponse.ok()
                            .bodyValueAndAwait(ExportByIdsResponse(result.rowsAffected))
                    is IdsNotFoundError ->
                        HttpStatus.BAD_REQUEST.let {
                                HttpError(
                                    status = it.value(),
                                    code = it.reasonPhrase,
                                    message =
                                        "Export request aborted because some entries where not found.",
                                    errors =
                                        result.ids.map {
                                            HttpError.Companion.ValidationError(
                                                field = "id",
                                                message = it.toString(),
                                            )
                                        },
                                )
                            }
                            .toServerResponse()
                    else -> HttpError.internalServerError.toServerResponse()
                }
            }
        }

    suspend fun deleteByIds(request: ServerRequest): ServerResponse =
        context(validator) {
            request.awaitValidatedBody<DeleteByIdsRequest>().let {
                when (val result = service.deleteByIds(it.ids)) {
                    is DeleteByIdsSuccess ->
                        ServerResponse.ok()
                            .bodyValueAndAwait(DeleteByIdsResponse(result.rowsAffected))
                    is IdsNotFoundError ->
                        HttpStatus.BAD_REQUEST.let {
                                HttpError(
                                    status = it.value(),
                                    code = it.reasonPhrase,
                                    message =
                                        "Delete request aborted because some entries where not found.",
                                    errors =
                                        result.ids.map {
                                            HttpError.Companion.ValidationError(
                                                field = "id",
                                                message = it.toString(),
                                            )
                                        },
                                )
                            }
                            .toServerResponse()
                    else -> HttpError.internalServerError.toServerResponse()
                }
            }
        }

    suspend fun flush(request: ServerRequest): ServerResponse =
        when (service.flush()) {
            Success -> ServerResponse.noContent().buildAndAwait()
            else -> HttpError.internalServerError.toServerResponse()
        }

    companion object {
        fun String.toLocalDateTime(): LocalDateTime? =
            runCatching {
                    URLDecoder.decode(this, StandardCharsets.UTF_8).let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    }
                }
                .getOrNull()
    }
}
