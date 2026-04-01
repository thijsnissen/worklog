package nl.thijsnissen.worklog.adapters.jira.client.http

import java.net.URI

data class JiraClientHttpConfig(val host: URI, val userEmail: String, val apiKey: String) {
    init {
        require(userEmail.isNotBlank()) { "Field userEmail cannot be blank." }
        require(apiKey.isNotBlank()) { "Field apiKey cannot be blank." }
    }
}
