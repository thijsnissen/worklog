package nl.thijsnissen.worklog.adapters.tempo.client.http

import java.net.URI
import java.time.Duration

data class TempoClientHttpConfig(
    val host: URI,
    val apiKey: String,
    val accountId: String,
    val maxConcurrentRequests: Int,
    val minRequestInterval: Duration,
) {
    init {
        require(apiKey.isNotBlank()) { "Field apiKey cannot be blank." }
        require(accountId.isNotBlank()) { "Field accountId cannot be blank." }
    }
}
