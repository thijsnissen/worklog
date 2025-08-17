package nl.thijsnissen.worklog

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

fun MockWebServer.dispatcher(fn: (RecordedRequest) -> MockResponse) {
    this.dispatcher =
        object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = fn(request)
        }
}
