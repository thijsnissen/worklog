plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.springdoc.openapi-gradle-plugin")
}

dependencies {
     implementation(versionCatalogUnsafe.findLibrary("springdoc.openapi.webflux.ui").get())
    implementation(versionCatalogUnsafe.findLibrary("springdoc.openapi.webflux.api").get())
}

openApi {
    apiDocsUrl.set("http://localhost:8080/api/v1/docs")
    outputDir.set(file("./../../../"))
}
