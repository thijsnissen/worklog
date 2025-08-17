package nl.thijsnissen.http.server

import org.springframework.beans.factory.BeanRegistrarDsl
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.server.HandlerStrategies

open class HttpServerWebExceptionHandlerBean :
    BeanRegistrarDsl({
        registerBean<HttpServerWebExceptionHandler>()
        registerBean<HandlerStrategies> { HandlerStrategies.withDefaults() }
    })

open class HttpServerCorsWebFilterBean(
    allowedOrigins: List<String>,
    allowedMethods: List<String>,
    allowedHeaders: List<String> = listOf("*"),
    allowCredentials: Boolean = false,
) :
    BeanRegistrarDsl({
        registerBean<CorsWebFilter> {
            val config =
                CorsConfiguration().apply {
                    this.allowCredentials = allowCredentials
                    this.allowedOrigins = allowedOrigins
                    this.allowedMethods = allowedMethods
                    this.allowedHeaders = allowedHeaders
                }

            val source =
                UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/**", config) }

            CorsWebFilter(source)
        }
    })
