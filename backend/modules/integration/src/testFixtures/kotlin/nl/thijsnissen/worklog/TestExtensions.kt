package nl.thijsnissen.worklog

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

fun LocalDateTime.urlEncode(): String? =
    runCatching { URLEncoder.encode(this.toString(), StandardCharsets.UTF_8) }.getOrNull()
