package nl.thijsnissen.http.server

import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validator
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

context(validator: Validator)
suspend inline fun <reified T : Any> ServerRequest.awaitValidatedBody(): T =
    this.awaitBody<T>().also {
        val v = validator.validate(it)

        if (v.isNotEmpty()) throw ConstraintViolationException(v)
    }
