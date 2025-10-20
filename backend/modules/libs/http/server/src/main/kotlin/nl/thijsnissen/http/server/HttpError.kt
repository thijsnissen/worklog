package nl.thijsnissen.http.server

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait

data class HttpError(
    @field:NotNull val status: Int,
    @field:NotBlank val code: String,
    @field:NotBlank val message: String,
    @field:NotNull val errors: List<ValidationError> = emptyList(),
    @field:NotNull val id: UUID = UUID.randomUUID(),
    @field:NotNull val timestamp: Instant = Instant.now(),
) {
    suspend fun toServerResponse(): ServerResponse {
        log.error { "HTTP Error $status $code (ref: $id): $message ${errors.joinToString()}" }

        return ServerResponse.status(status).bodyValueAndAwait(this)
    }

    companion object {
        private val log = KotlinLogging.logger {}

        data class ValidationError(
            @field:NotBlank val field: String,
            @field:NotNull val message: String,
        ) {
            override fun toString(): String = "'${this.field}: ${this.message}'"
        }

        val internalServerError: HttpError =
            HttpStatus.INTERNAL_SERVER_ERROR.let {
                HttpError(
                    status = it.value(),
                    code = it.reasonPhrase,
                    message = "${it.value()} ${it.reasonPhrase}",
                )
            }
    }
}
