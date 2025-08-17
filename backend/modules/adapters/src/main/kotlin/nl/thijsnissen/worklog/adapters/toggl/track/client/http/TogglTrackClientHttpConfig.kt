package nl.thijsnissen.worklog.adapters.toggl.track.client.http

import java.net.URI
import java.time.ZoneId

data class TogglTrackClientHttpConfig(val host: URI, val apiToken: String, val timeZone: ZoneId)
