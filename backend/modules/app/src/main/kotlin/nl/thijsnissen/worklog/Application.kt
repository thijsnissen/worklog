package nl.thijsnissen.worklog

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootConfiguration @EnableAutoConfiguration class Application

fun main(args: Array<String>) {
    runCatching {
            dotenv {
                directory = "./../../../.env"
                systemProperties = true
            }
        }
        .onFailure { println("No .env file found.") }

    runApplication<Application>(*args) {
        addInitializers(
            OnStartupMessageLive(),

            // Service
            WorklogServiceImplLive(),

            // Adapters
            ApiHttpWorklogLive(),
            OpenApiSpecLive(),
            JiraClientHttpLive(),
            TempoClientHttpLive(),
            TogglTrackClientHttpConfigLive(),
            TogglTrackClientHttpLive(),
            WorklogRepositoryH2Live(),

            // Libs
            HttpClientLive(),
            HttpServerWebExceptionHandlerLive(),
            HttpServerCorsWebFilterLive(),
        )
    }
}
