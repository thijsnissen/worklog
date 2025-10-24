package nl.thijsnissen.worklog.adapters.jira.client.http.dto

data class BulkFetchRequest(val issueIdsOrKeys: List<String>, val fields: List<String> = listOf(""))
