package nl.thijsnissen.worklog.adapters.api.http.worklog.dto

import jakarta.validation.constraints.NotNull

data class DeleteByIdsResponse(@field:NotNull val rowsAffected: Long)
