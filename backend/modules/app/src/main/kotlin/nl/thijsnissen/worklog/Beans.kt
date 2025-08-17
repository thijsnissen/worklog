package nl.thijsnissen.worklog

import nl.thijsnissen.http.client.HttpClientDefaultWebClientBean
import nl.thijsnissen.http.openapi.OpenApi
import nl.thijsnissen.http.openapi.OpenApiSpecBean
import nl.thijsnissen.http.server.HttpServerCorsWebFilterBean
import nl.thijsnissen.http.server.HttpServerWebExceptionHandlerBean
import nl.thijsnissen.worklog.adapters.api.http.worklog.Endpoints
import nl.thijsnissen.worklog.adapters.api.http.worklog.Handler
import nl.thijsnissen.worklog.adapters.jira.client.http.JiraClientHttp
import nl.thijsnissen.worklog.adapters.jira.client.http.JiraClientHttpConfig
import nl.thijsnissen.worklog.adapters.tempo.client.http.TempoClientHttp
import nl.thijsnissen.worklog.adapters.tempo.client.http.TempoClientHttpConfig
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.TogglTrackClientHttp
import nl.thijsnissen.worklog.adapters.toggl.track.client.http.TogglTrackClientHttpConfig
import nl.thijsnissen.worklog.adapters.worklog.repository.h2.WorklogRepositoryH2
import nl.thijsnissen.worklog.adapters.worklog.repository.h2.WorklogRepositoryH2Config
import nl.thijsnissen.worklog.core.WorklogServiceImpl
import nl.thijsnissen.worklog.core.WorklogServiceImplConfig
import org.springframework.beans.factory.BeanRegistrarDsl
import org.springframework.boot.context.properties.bind.Binder

object OnStartupMessageLive : BeanRegistrarDsl({ registerBean<OnStartupMessage>() })

// Service
object WorklogServiceImplLive :
    BeanRegistrarDsl({
        registerBean<WorklogServiceImpl>()
        registerBean<WorklogServiceImplConfig> {
            Binder.get(env)
                .bind("service.worklog-service-impl.config", WorklogServiceImplConfig::class.java)
                .get()
        }
    })

// Adapters
object ApiHttpWorklogLive :
    BeanRegistrarDsl({
        registerBean<Endpoints>()
        registerBean<Handler>()
        registerBean { bean<Endpoints>().endpoints() }
    })

object OpenApiSpecLive :
    OpenApiSpecBean(
        title = "Worklog",
        version = "1.0.0",
        servers =
            Endpoints.servers() +
                OpenApi.OpenApiServer(
                    url = "http://localhost:8080",
                    description = "Actuator Server",
                ),
        items = Endpoints.spec(),
    )

object JiraClientHttpLive :
    BeanRegistrarDsl({
        registerBean<JiraClientHttp>()
        registerBean<JiraClientHttpConfig> {
            Binder.get(env)
                .bind("adapters.jira-client-http.config", JiraClientHttpConfig::class.java)
                .get()
        }
    })

object TempoClientHttpLive :
    BeanRegistrarDsl({
        registerBean<TempoClientHttp>()
        registerBean<TempoClientHttpConfig> {
            Binder.get(env)
                .bind("adapters.tempo-client-http.config", TempoClientHttpConfig::class.java)
                .get()
        }
    })

object TogglTrackClientHttpConfigLive :
    BeanRegistrarDsl({
        registerBean<TogglTrackClientHttpConfig> {
            Binder.get(env)
                .bind(
                    "adapters.toggl-track-client-http.config",
                    TogglTrackClientHttpConfig::class.java,
                )
                .get()
        }
    })

object TogglTrackClientHttpLive : BeanRegistrarDsl({ registerBean<TogglTrackClientHttp>() })

object WorklogRepositoryH2Live :
    BeanRegistrarDsl({
        registerBean<WorklogRepositoryH2>()
        registerBean<WorklogRepositoryH2Config> {
            Binder.get(env)
                .bind(
                    "adapters.worklog-repository-h2.config",
                    WorklogRepositoryH2Config::class.java,
                )
                .get()
        }
    })

// Libs
object HttpClientLive : HttpClientDefaultWebClientBean("libs.http.client.config")

object HttpServerWebExceptionHandlerLive : HttpServerWebExceptionHandlerBean()

object HttpServerCorsWebFilterLive :
    HttpServerCorsWebFilterBean(
        allowedOrigins = listOf("http://localhost:8080", "http://localhost:3000"),
        allowedMethods = listOf("GET", "POST", "DELETE"),
    )
