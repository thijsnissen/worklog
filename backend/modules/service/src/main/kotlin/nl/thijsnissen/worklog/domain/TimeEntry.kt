package nl.thijsnissen.worklog.domain

import java.time.Duration
import java.time.LocalDateTime

data class TimeEntry(
    val startInclusive: LocalDateTime,
    val endInclusive: LocalDateTime,
    val duration: Duration,
    val description: Description,
)
