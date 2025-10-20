package nl.thijsnissen.http.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import java.util.concurrent.TimeUnit
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry

class HttpClient(val config: HttpClientConfig, val builder: WebClient.Builder) {
    fun defaultWebClient(): WebClient {
        val httpClient =
            HttpClient.create()
                .option(
                    ChannelOption.CONNECT_TIMEOUT_MILLIS,
                    config.timeout.connection.toMillis().toInt(),
                )
                .responseTimeout(config.timeout.response)
                .doOnConnected {
                    it.addHandlerLast(
                            ReadTimeoutHandler(
                                config.timeout.read.toMillis(),
                                TimeUnit.MILLISECONDS,
                            )
                        )
                        .addHandlerLast(
                            WriteTimeoutHandler(
                                config.timeout.write.toMillis(),
                                TimeUnit.MILLISECONDS,
                            )
                        )
                }

        val retry: Retry =
            Retry.backoff(config.retry.attempts, config.retry.minBackoff)
                .maxBackoff(config.retry.maxBackoff)
                .jitter(config.retry.jitter)
                .filter { shouldRetry(it) }
                .doBeforeRetry { s ->
                    log.warn {
                        "Retrying request due to ${s.failure().cause.toString()} (attempt ${s.totalRetries() + 1} of ${config.retry.attempts}): ${s.failure().message}"
                    }
                }

        return builder
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .filter { request, next -> next.exchange(request).retryWhen(retry) }
            .build()
    }

    companion object {
        private val log = KotlinLogging.logger {}

        fun shouldRetry(error: Throwable): Boolean =
            when (error) {
                is WebClientRequestException -> true
                is WebClientResponseException -> error.statusCode.is5xxServerError
                else -> false
            }
    }
}
