package nl.thijsnissen.worklog.adapters.api.http.worklog.dto

import jakarta.validation.constraints.NotNull

data class ImportInRangeResponse(@field:NotNull val rowsAffected: Long)
