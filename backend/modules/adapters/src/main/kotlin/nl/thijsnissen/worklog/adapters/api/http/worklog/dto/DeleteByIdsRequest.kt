package nl.thijsnissen.worklog.adapters.api.http.worklog.dto

import jakarta.validation.constraints.NotEmpty
import java.util.*

data class DeleteByIdsRequest(@field:NotEmpty(message = "ids cannot be empty") val ids: List<UUID>)
