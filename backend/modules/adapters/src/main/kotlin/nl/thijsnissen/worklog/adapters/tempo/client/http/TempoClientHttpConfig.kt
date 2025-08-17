package nl.thijsnissen.worklog.adapters.tempo.client.http

import java.net.URI

data class TempoClientHttpConfig(
    val host: URI,
    val apiKey: String,
    val accountId: String,
    val maxConcurrentRequests: Int,
)
