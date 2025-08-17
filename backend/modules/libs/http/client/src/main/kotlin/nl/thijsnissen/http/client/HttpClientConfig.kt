package nl.thijsnissen.http.client

import java.time.Duration

data class HttpClientConfig(val timeout: TimeoutConfig, val retry: RetryConfig)

data class TimeoutConfig(
    val connection: Duration,
    val read: Duration,
    val write: Duration,
    val response: Duration,
)

data class RetryConfig(
    val attempts: Long,
    val minBackoff: Duration,
    val maxBackoff: Duration,
    val jitter: Double,
)
