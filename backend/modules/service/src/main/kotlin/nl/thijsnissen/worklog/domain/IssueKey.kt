package nl.thijsnissen.worklog.domain

import kotlin.text.trim

@JvmInline
value class IssueKey(val value: String) {
    infix fun eqv(that: IssueKey): Boolean =
        this.value.trim().lowercase() == that.value.trim().lowercase()
}
