plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.jib)
    implementation(libs.kotlin)
    implementation(libs.kotlin.spring)
    implementation(libs.ktfmt)
    implementation(libs.spring.boot)
    implementation(libs.springdoc.openapi.plugin)
}
