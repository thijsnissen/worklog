import utils.versionCatalogUnsafe

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
}

dependencies {
    implementation(versionCatalogUnsafe.findLibrary("jackson.module.kotlin").get())
    implementation(versionCatalogUnsafe.findLibrary("kotlinx.coroutines.reactor").get())
    implementation(versionCatalogUnsafe.findLibrary("reactor.kotlin.extensions").get())
    implementation(versionCatalogUnsafe.findLibrary("spring.boot.starter.actuator").get())
    implementation(versionCatalogUnsafe.findLibrary("spring.boot.starter.flyway").get())
    implementation(versionCatalogUnsafe.findLibrary("spring.boot.starter.r2dbc").get())
    implementation(versionCatalogUnsafe.findLibrary("spring.boot.starter.validation").get())
    implementation(versionCatalogUnsafe.findLibrary("spring.boot.starter.webflux").get())
    implementation(versionCatalogUnsafe.findLibrary("spring.boot.starter.webclient").get())
    runtimeOnly(versionCatalogUnsafe.findLibrary("netty.resolver.dns.native.macos").get()) {
        artifact { classifier = "osx-aarch_64" }
    }
    testImplementation(versionCatalogUnsafe.findLibrary("reactor.test").get())
    testImplementation(versionCatalogUnsafe.findLibrary("spring.boot.starter.test").get())
}
