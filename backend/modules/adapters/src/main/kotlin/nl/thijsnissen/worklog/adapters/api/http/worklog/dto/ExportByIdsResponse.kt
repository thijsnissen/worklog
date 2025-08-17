package nl.thijsnissen.worklog.adapters.api.http.worklog.dto

import jakarta.validation.constraints.NotNull

data class ExportByIdsResponse(@field:NotNull val rowsAffected: Long)
