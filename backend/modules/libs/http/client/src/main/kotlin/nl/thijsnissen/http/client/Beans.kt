package nl.thijsnissen.http.client

import org.springframework.beans.factory.BeanRegistrarDsl
import org.springframework.boot.context.properties.bind.Binder

open class HttpClientDefaultWebClientBean(config: String) :
    BeanRegistrarDsl({
        registerBean<HttpClient>()
        registerBean<HttpClientConfig> {
            Binder.get(env).bind(config, HttpClientConfig::class.java).get()
        }
        registerBean { bean<HttpClient>().defaultWebClient() }
    })
