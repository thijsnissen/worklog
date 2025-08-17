package nl.thijsnissen.worklog.adapters.api.http.worklog.dto

import jakarta.validation.constraints.NotNull
import nl.thijsnissen.worklog.domain.Worklog as DomainWorkLog

data class GetAllResponse(@field:NotNull val worklogs: List<Worklog>) {
    companion object {
        fun fromDomain(worklogs: List<DomainWorkLog>): GetAllResponse =
            GetAllResponse(worklogs.map(Worklog::fromDomain))
    }
}
