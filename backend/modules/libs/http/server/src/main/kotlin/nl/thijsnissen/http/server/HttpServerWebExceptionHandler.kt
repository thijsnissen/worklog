package nl.thijsnissen.http.server

import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.result.view.ViewResolver
import org.springframework.web.server.CoWebExceptionHandler
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange

class HttpServerWebExceptionHandler(val strategies: HandlerStrategies) :
    CoWebExceptionHandler(), Ordered {
    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    override suspend fun coHandle(exchange: ServerWebExchange, ex: Throwable) {
        log.error(ex.message, ex)

        when (ex) {
                is ConstraintViolationException -> validationHandler(ex.constraintViolations)
                is ResponseStatusException -> apiErrorHandler(ex.statusCode, ex.reason)
                else -> defaultErrorHandler()
            }
            .writeTo(
                exchange,
                object : ServerResponse.Context {
                    override fun messageWriters(): List<HttpMessageWriter<*>> =
                        strategies.messageWriters()

                    override fun viewResolvers(): List<ViewResolver> = strategies.viewResolvers()
                },
            )
            .awaitSingleOrNull()
    }

    suspend fun validationHandler(errors: Set<ConstraintViolation<*>>): ServerResponse =
        HttpStatus.BAD_REQUEST.let {
                HttpError(
                    status = it.value(),
                    code = it.reasonPhrase,
                    message = "Validation Failed",
                    errors =
                        errors.map { e ->
                            HttpError.Companion.ValidationError(
                                e.propertyPath.toString(),
                                e.message ?: "Invalid",
                            )
                        },
                )
            }
            .toServerResponse()

    suspend fun apiErrorHandler(status: HttpStatusCode, reason: String?): ServerResponse =
        HttpStatus.valueOf(status.value())
            .let { HttpError(status = it.value(), code = it.reasonPhrase, message = reason ?: "") }
            .toServerResponse()

    suspend fun defaultErrorHandler(): ServerResponse =
        HttpStatus.INTERNAL_SERVER_ERROR.let {
                HttpError(
                    status = it.value(),
                    code = it.reasonPhrase,
                    message = "An unexpected error occurred.",
                )
            }
            .toServerResponse()

    companion object {
        private val log = LoggerFactory.getLogger(HttpServerWebExceptionHandler::class.java)
    }
}
