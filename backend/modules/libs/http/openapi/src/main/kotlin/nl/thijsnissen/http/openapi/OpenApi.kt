package nl.thijsnissen.http.openapi

import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.servers.Server
import kotlin.reflect.KClass
import org.springframework.http.HttpStatus

object OpenApi {
    data class OpenApiPathItem(val value: PathItem, val schemas: List<KClass<*>>) {
        companion object {
            fun pathItem(vararg operations: Pair<HttpMethod, OpenApiOperation>): OpenApiPathItem {
                val operations = operations.toList()
                val schemas = operations.flatMap { (_, o) -> o.schemas }
                val pathItem =
                    PathItem().apply {
                        operations.forEach { (m, o) -> operation(m.toOas(), o.value) }
                    }

                return OpenApiPathItem(pathItem, schemas)
            }

            enum class HttpMethod {
                GET,
                POST,
                PUT,
                PATCH,
                DELETE,
                HEAD,
                OPTIONS,
                TRACE,
            }

            fun HttpMethod.toOas(): PathItem.HttpMethod =
                when (this) {
                    HttpMethod.GET -> PathItem.HttpMethod.GET
                    HttpMethod.POST -> PathItem.HttpMethod.POST
                    HttpMethod.PUT -> PathItem.HttpMethod.PUT
                    HttpMethod.PATCH -> PathItem.HttpMethod.PATCH
                    HttpMethod.DELETE -> PathItem.HttpMethod.DELETE
                    HttpMethod.HEAD -> PathItem.HttpMethod.HEAD
                    HttpMethod.OPTIONS -> PathItem.HttpMethod.OPTIONS
                    HttpMethod.TRACE -> PathItem.HttpMethod.TRACE
                }
        }
    }

    data class OpenApiItem(val path: String, val item: OpenApiPathItem)

    data class OpenApiOperation(val value: Operation, val schemas: List<KClass<*>>) {
        companion object {
            inline fun <reified Req : Any> operation(
                operationId: String,
                summary: String,
                tag: String,
                responses: List<OpenApiResponse>,
                parameters: List<OpenApiParameter> = emptyList(),
            ): OpenApiOperation {
                val operation =
                    Operation().apply {
                        this.operationId = operationId
                        this.summary = summary
                        this.tags = listOf(tag)
                        this.responses =
                            responses.fold(ApiResponses()) { acc, (status, response) ->
                                acc.addApiResponse(status.value().toString(), response)
                            }
                        this.parameters =
                            parameters.map {
                                Parameter().apply {
                                    name = it.name
                                    `in` = it.type
                                    description = it.description
                                    required = it.required
                                    schema = it.schema
                                }
                            }

                        if (!isAny<Req>()) {
                            this.requestBody =
                                RequestBody().apply {
                                    content = jsonContent(Req::class)
                                    required(true)
                                }
                        }
                    }

                return OpenApiOperation(
                    operation,
                    (responses.map { it.schema } + Req::class.takeUnless { isAny<Req>() })
                        .filterNotNull(),
                )
            }
        }
    }

    data class OpenApiResponse(
        val status: HttpStatus,
        val response: ApiResponse,
        val schema: KClass<*>?,
    ) {
        companion object {
            inline fun <reified Res : Any> response(
                status: HttpStatus,
                description: String? = null,
            ): OpenApiResponse {
                val response =
                    ApiResponse().apply {
                        this.description = description ?: status.reasonPhrase

                        if (!isAny<Res>() && status != HttpStatus.NO_CONTENT) {
                            this.content = jsonContent(Res::class)
                        }
                    }

                return OpenApiResponse(status, response, Res::class.takeUnless { isAny<Res>() })
            }
        }
    }

    data class OpenApiParameter(
        val name: String,
        val description: String,
        val type: String,
        val required: Boolean,
        val schema: Schema<*>,
    ) {
        companion object {
            fun queryParameter(
                name: String,
                description: String,
                required: Boolean = false,
            ): OpenApiParameter =
                OpenApiParameter(name, description, "query", required, StringSchema())

            fun pathParameter(
                name: String,
                description: String,
                required: Boolean = false,
            ): OpenApiParameter =
                OpenApiParameter(name, description, "path", required, StringSchema())

            fun headerParameter(
                name: String,
                description: String,
                required: Boolean = false,
            ): OpenApiParameter =
                OpenApiParameter(name, description, "header", required, StringSchema())

            fun cookieParameter(
                name: String,
                description: String,
                required: Boolean = false,
            ): OpenApiParameter =
                OpenApiParameter(name, description, "cookie", required, StringSchema())
        }
    }

    data class OpenApiServer(val url: String, val description: String) {
        private val self = this

        fun toServer(): Server =
            Server().apply {
                this.url = self.url
                this.description = self.description
            }
    }

    fun spec(
        title: String,
        version: String,
        servers: List<OpenApiServer>,
        items: List<OpenApiItem>,
    ): OpenAPI =
        OpenAPI().apply {
            this.info =
                Info().apply {
                    this.title = title
                    this.version = version
                }

            this.servers = servers.map { it.toServer() }

            this.paths =
                items.fold(Paths()) { acc, (path, item) -> acc.addPathItem(path, item.value) }

            this.components =
                Components().apply {
                    schemas =
                        items
                            .flatMap { it.item.schemas }
                            .map { schemaResolver(it) }
                            .reduce { acc, map -> acc + map }
                }
        }

    inline fun <reified T : Any> isAny(): Boolean = T::class == Any::class

    private fun schemaResolver(kClass: KClass<*>): Map<String, Schema<*>> =
        ModelConverters.getInstance()
            .readAllAsResolvedSchema(AnnotatedType().type(kClass.java))
            .let { rs ->
                (rs.referencedSchemas ?: emptyMap()) +
                    requireNotNull(
                        kClass.simpleName?.let { name -> rs?.schema?.let { name to it } }
                    ) {
                        "Cannot resolve schema for ${kClass.simpleName}"
                    }
            }

    inline fun <reified T : Any> jsonContent(kClass: KClass<T>): Content =
        Content().apply {
            addMediaType(
                "application/json",
                MediaType().apply {
                    this.schema =
                        Schema<Any>().`$ref`("#/components/schemas/${kClass.simpleName ?: ""}")
                },
            )
        }
}
