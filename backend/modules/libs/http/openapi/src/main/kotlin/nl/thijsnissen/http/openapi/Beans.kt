package nl.thijsnissen.http.openapi

import org.springframework.beans.factory.BeanRegistrarDsl

open class OpenApiSpecBean(
    title: String,
    version: String,
    servers: List<OpenApi.OpenApiServer>,
    items: List<OpenApi.OpenApiItem>,
) :
    BeanRegistrarDsl({
        registerBean {
            OpenApi.spec(title = title, version = version, servers = servers, items = items)
        }
    })
